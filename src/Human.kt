import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

open class Human(
    private var fullName: String,
    private var age: Int,
    protected var speed: Double // свойство, Kotlin сам сделает getSpeed()/setSpeed()
) {
    protected var x: Double = 0.0
    protected var y: Double = 0.0

    // Публичные readonly-свойства для main()
    val posX: Double get() = x
    val posY: Double get() = y

    fun getFullName(): String = fullName
    fun setFullName(name: String) { fullName = name }

    fun getAge(): Int = age
    fun setAge(a: Int) { age = a }

    open fun move(deltaTime: Double = 1.0) {
        val angle = Random.nextDouble(0.0, 2 * PI)
        val dx = speed * deltaTime * cos(angle)
        val dy = speed * deltaTime * sin(angle)
        x += dx
        y += dy
    }

    override fun toString(): String {
        return "${this::class.simpleName}(ФИО='$fullName', Возраст=$age, Скорость=$speed, " +
                "Координаты=(${String.format("%.2f", posX)}, ${String.format("%.2f", posY)}))"
    }
}
