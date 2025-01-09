# Set working directory.
setwd("/home/fabian/programming/operating-systems/portfolio2/statistics")

# Create scatter plots for task 2-4
latencies <- as.numeric(readLines("../aufgabe2/latencies.txt"))
plot(latencies, main = "Scatter Plot (Semaphore)", xlab = "Durchlauf", ylab = "Minimale Latenz")

latencies <- as.numeric(readLines("../aufgabe3/between_threads/latencies.txt"))
plot(latencies, main = "Scatter Plot (ZeroMQ In-Process)", xlab = "Durchlauf", ylab = "Minimale Latenz")

latencies <- as.numeric(readLines("../aufgabe3/between_processes/latencies.txt"))
plot(latencies, main = "Scatter Plot (ZeroMQ Inter-Process)", xlab = "Durchlauf", ylab = "Minimale Latenz")

latencies <- as.numeric(readLines("../aufgabe4/latencies.txt"))
plot(latencies, main = "Scatter Plot (ZeroMQ between Docker containers)", xlab = "Durchlauf", ylab = "Minimale Latenz")
