import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class Human(
    private var fullName: String,
    private var age: Int,
    private var speed: Double // скорость (м/с)
) {
    private var x: Double = 0.0
    private var y: Double = 0.0

    // Геттеры / сеттеры
    fun getFullName(): String = fullName
    fun setFullName(name: String) { fullName = name }

    fun getAge(): Int = age
    fun setAge(a: Int) { age = a }

    fun getSpeed(): Double = speed
    fun setSpeed(s: Double) { speed = s }

    fun getX(): Double = x
    fun getY(): Double = y

    // Random Walk
    fun move(deltaTime: Double = 1.0) {
        val angle = Random.nextDouble(0.0, 2 * PI)
        val dx = speed * deltaTime * cos(angle)
        val dy = speed * deltaTime * sin(angle)
        x += dx
        y += dy
    }

    override fun toString(): String {
        return "Human(ФИО='$fullName', Возраст=$age, Скорость=$speed, " +
                "Координаты=(${String.format("%.2f", x)}, ${String.format("%.2f", y)}))"
    }
}
