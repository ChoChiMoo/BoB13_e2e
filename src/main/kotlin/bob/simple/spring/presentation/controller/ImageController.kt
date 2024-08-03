package bob.simple.spring.presentation.controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.util.Base64
import javax.imageio.ImageIO

@RestController
class ImageController {

    // 이미지를 Base64 형식으로 변환하는 함수
    fun bufferedImageToBase64(image: BufferedImage): String {
        val outputStream = java.io.ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.getEncoder().encodeToString(imageBytes)
    }

    // 입력된 문자열을 바탕으로 이미지 경로 리스트를 생성하는 함수
    fun getImagePathsForInput(input: String): List<List<String>> {
        val images = input.map {
            "src/main/resources/keypadimages/_${it}.png"
        }

        // 2행 6열로 분할
        return listOf(
            images.subList(0, 6),
            images.subList(6, 12)
        )
    }

    // 여러 이미지를 하나의 이미지로 합성하는 함수 (가로로 합침)
    fun combineImages(imagePaths: List<String>): BufferedImage {
        val images = imagePaths.map { ImageIO.read(File(it)) }

        val totalWidth = images.sumOf { it.width }
        val maxHeight = images.maxOf { it.height }

        val combinedImage = BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_ARGB)
        val g: Graphics = combinedImage.graphics

        var currentWidth = 0
        for (image in images) {
            g.drawImage(image, currentWidth, 0, null)
            currentWidth += image.width
        }

        g.dispose()
        return combinedImage
    }

    // 2행 6열의 이미지를 합성하는 함수
    fun combineImagesInGrid(imagePathsGrid: List<List<String>>): BufferedImage {
        val rowImages = imagePathsGrid.map { combineImages(it) }

        val totalHeight = rowImages.sumOf { it.height }
        val maxWidth = rowImages.maxOf { it.width }

        val finalImage = BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_ARGB)
        val g: Graphics = finalImage.graphics

        var currentHeight = 0
        for (rowImage in rowImages) {
            g.drawImage(rowImage, 0, currentHeight, null)
            currentHeight += rowImage.height
        }

        g.dispose()
        return finalImage
    }

    // API 엔드포인트 설정
    @GetMapping("/keypad/encodedImage")
    fun getEncodedKeypadImage(@RequestParam input: String): String {
        // 입력 길이 확인: 12자리가 아니면 오류 메시지 반환
        if (input.length != 12) {
            return "Error: Input must be exactly 12 characters long."
        }
        // 2행 6열의 이미지 경로 리스트 준비
        val imagePathsGrid = getImagePathsForInput(input)

        // 이미지를 2행 6열로 합성
        val combinedImage = combineImagesInGrid(imagePathsGrid)

        // Base64로 인코딩하여 반환
        return bufferedImageToBase64(combinedImage)
    }
}
