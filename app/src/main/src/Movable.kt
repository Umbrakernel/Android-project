interface Movable {
    var x: Double
    var y: Double
    var speed: Double

    fun move(deltaTime: Double = 1.0)
}