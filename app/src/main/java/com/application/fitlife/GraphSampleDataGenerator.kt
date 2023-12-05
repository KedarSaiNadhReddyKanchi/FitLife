package com.application.fitlife

class GraphSampleDataGenerator {

    fun dailyScoresArrayData(): IntArray {
        return intArrayOf(84,87,85,88,99,96,75,88,87,95,92,81,97,76,96,97,80,100,77,77,79,91,96,78,76,98,82,98,83)
    }

    fun heartRatesArrayData(): IntArray {
        return intArrayOf(72,67,78,66,66, 78, 78, 61, 67, 62, 76, 71, 76, 69, 61, 60, 62, 78, 79, 78, 72, 70, 70, 72, 60, 71, 79, 78, 70)
    }

    fun respiratoryRatesArrayData(): IntArray {
        return intArrayOf(13,18,17,18,17,14,13,12,17,17,13,12,13,15,17,14,14,14,12,12,13,18,15,15,16,12,12,15,16)
    }

    fun userWeightArrayData(): IntArray {
        return intArrayOf(90,90,90,90,90,90,90,90,89,89,89,89,88,88,88,88,88,87,87,87,87,87,86,86,86,85,85,85,85)
    }

    fun createAndPopulateArray(): IntArray {
        // Step 1: Define an array to store integers
        val intArray = IntArray(5) // You can change the size as needed

        // Step 2: Write a loop to push a new value into the array
        for (i in intArray.indices) {
            // You can replace the logic here to get the value you want to push
            val newValue = i * 2
            intArray[i] = newValue
        }

        // Step 3: Return the populated array
        return intArray
    }
}