fun main() {
    val humans = listOf(
        Human("Иванов Иван Иванович", 25, 1.2),
        Human("Петров Петр Петрович", 30, 1.5),
        Human("Сидоров Сидор Сидорович", 20, 1.0),
        Driver("Смирнов Алексей Сергеевич", 28, 2.0, 1.0, 0.0) // двигается по оси X
    )

    val simulationTime = 5 // секунд
    val deltaTime = 1.0    // шаг времени (1 секунда)

    println("=== Начало симуляции ===")

    // Поток для каждого Human/Driver
    val threads = humans.map { human ->
        Thread {
            for (t in 1..simulationTime) {
                human.move(deltaTime)
                println("[${Thread.currentThread().name}] $human")
                Thread.sleep((deltaTime * 1000).toLong())
            }
        }
    }

    // Запуск всех потоков
    threads.forEach { it.start() }
    // Ждём завершения всех
    threads.forEach { it.join() }

    println("=== Конец симуляции ===")
}
