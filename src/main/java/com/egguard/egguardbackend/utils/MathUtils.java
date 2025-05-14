package com.egguard.egguardbackend.utils;

/**
 * Utility class for mathematical operations
 */
public class MathUtils {
    
    /**
     * Calculates the Euclidean distance between two points in a 2D plane
     * 
     * @param x1 x-coordinate of the first point
     * @param y1 y-coordinate of the first point
     * @param x2 x-coordinate of the second point
     * @param y2 y-coordinate of the second point
     * @return the Euclidean distance between the two points
     */
    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
} 