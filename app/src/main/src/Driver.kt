class Driver(
    fullName: String,
    age: Int,
    speed: Double,
    private val directionX: Double,
    private val directionY: Double
) : Human(fullName, age, speed) {

    override fun move(deltaTime: Double) {
        val dx = speed * deltaTime * directionX
        val dy = speed * deltaTime * directionY
        x += dx
        y += dy
    }
}

