package com.example.getiiabotpapp.imagetextrecognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.getiiabotpapp.databinding.TextRecognitionActivityBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TextRecognitionActivity : AppCompatActivity() {

    private lateinit var binding: TextRecognitionActivityBinding
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TextRecognitionActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        // Initialize the camera and text recognizer
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build()
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val imageAnalysis = setupImageAnalysis()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this as LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun setupImageAnalysis(): ImageAnalysis {
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val analyzer = ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val scannedText = visionText.text
                        if (scannedText.isNotEmpty()) {
                            val separatedStrings = scannedText.split("\n")

                            Log.d("moises_print", "valid: $scannedText  separatedStrings= ${separatedStrings.size}")
                            for(text : String in separatedStrings)
                            if (isValidVinNumberInput(text)) {
                                runOnUiThread {
                                    binding.textView.text = text
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Handle recognition failure
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }

        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, analyzer)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun isValidVinNumberInput(input: String): Boolean {
        val pattern = "[A-HJ-NPR-Z0-9]{17}"
//        val pattern = "(?=.*\\d|=.*[A-Z])(?=.*[A-Z])[A-Z0-9]{17}"
        return Regex(pattern).matches(input)
    }

    private fun isValidInputTest(input: String) {
        val input = "457-87-5786"
        val pattern = "\\d{3}-\\d{2}-\\d{4}"
        val isMatch = Regex(pattern).matches(input)
        println(isMatch) // true
    }

    /* Regular Expressions(Regex) in Kotlin
    https://betulnecanli.medium.com/regular-expressions-regex-in-kotlin-a2eaeb2cd113#:~:text=Here%20are%20some%20examples%20of%20how%20to%20use%20regular%20expressions,expression%20in%20the%20input%20string.
    A regular expression consists of a pattern, which is a sequence of characters, and one or more optional flags that modify
    the search. The pattern is used to match against a string, and the result of the match is either a success or failure.

    Regex — — — -> Meaning
    ‘.’ — — — -> Matches any single character.
    ‘?’ — — — -> Matches the preceding element once or not at all.
    ‘+’ — — — -> Matches the preceding element once or more times.
    ‘*’ — — — -> Matches the preceding element zero or more times.
    ‘^’ — — — -> Matches the starting position within the string.
    ‘$’ — — — -> Matches the ending position within the string.
    ‘|’ — — — -> Alternation operator.
    ‘[abc]’ — — — -> Matches a or b, or c.
    ‘[a-c]’ — — — -> Range; matches a or b, or c.
    ‘[^abc]’ — — — -> Negation, matches everything except a, or b, or c.
    ‘\s’ — — — -> Matches white space character.
    ‘\w’ — — — -> Matches a word character; equivalent to [a-zA-Z_0–9]
    Here are some examples of regular expressions:
    ‘a.b’ matches any string that contains an "a" followed by any character followed by a "b", such as "a1b" or "a_b".
    ‘[abc]’ matches any string that contains an "a", "b", or "c".
    ‘\d’ matches any digit (0-9).
    ‘^[A-Z][a-z]*’matches any string that starts with an uppercase letter, followed by any number of lowercase letters.

    Regular expressions are useful because they provide a concise and flexible way to match patterns in strings. Some common uses of regular expressions include:
Validation: Regular expressions can be used to validate that a string meets certain criteria, such as checking that an email address is in the correct format.
Search and Replace: Regular expressions can be used to search for specific patterns in a string and replace them with new text. This can be useful for tasks such as removing unwanted characters from a string or formatting text in a certain way.
Text Parsing: Regular expressions can be used to extract specific information from a string, such as extracting all the URLs from a webpage.
Data Cleaning: Regular expressions can be used to clean up and standardize data, such as removing leading and trailing white space, converting all text to lowercase, and removing duplicate entries.
String manipulation: Regular expressions can be used to manipulate strings in various ways, such as finding a specific character, replacing a set of characters with new characters, and so on.
String Validation: Regular expressions can be used to validate if a string matches a certain pattern, such as checking if a string is a valid date, phone number, etc.
*/
}