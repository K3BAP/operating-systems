# Set working directory.
setwd("/home/fabian/programming/operating-systems/portfolio2/statistics")

# Read the latencies from the text file
latencies <- as.numeric(readLines("../aufgabe1/latencies.txt"))

# Summary statistics
summary(latencies)

# Clip outliers (99th percentile)
threshold <- quantile(latencies, 0.99)
filtered_latencies <- latencies[latencies <= threshold]

# Compute mean and 95% confidence interval
mean_latency <- mean(filtered_latencies)
sd_latency <- sd(filtered_latencies)
margin <- qt(0.975,df=length(filtered_latencies)-1)*sd_latency/sqrt(length(filtered_latencies))
ci_upper <- mean_latency + margin
ci_lower <- mean_latency - margin


# Bootstrap transformation to calculate mean minimum and confidence interval for minimum
if(!require(boot)){install.packages("boot")}
library(boot)

min_function <- function(data, indices) {
  return(min(data[indices]))
}

boot_results <- boot(data = filtered_latencies, statistic = min_function, R = 1000)

ci_perc <- boot.ci(boot_results, type = "perc")
min_ci_upper <- ci_perc$percent[5]
min_ci_lower <- ci_perc$percent[4]




# Plot the distribution
hist(filtered_latencies, main = paste0("Latency Distribution (n=", length(latencies), " samples)"), xlab = "Latency (ns)")

# Plot mean and confidence interval
abline(v = mean_latency, col = "blue", lwd = 2)
abline(v = ci_upper, col = "skyblue", lwd = 2)
abline(v = ci_lower, col = "skyblue", lwd = 2)

# Plot confidence interval for minimum
abline(v = min_ci_lower, col = "red", lwd = 2)
abline(v = min_ci_upper, col = "red", lwd = 2)

