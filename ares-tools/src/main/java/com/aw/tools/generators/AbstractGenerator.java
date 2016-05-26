package com.aw.tools.generators;

/**
 * Base class for generators of test data
 *
 *
 *
 */
public abstract class AbstractGenerator {

	/**
	 * When computing random values with probabilities, this value is used to represent the probability
	 * of none of the other values occurring
	 */
	protected static final double REMAINDER = -1;

	/**
	 * Get a random number between the two values
	 *
	 * @param min min value
	 * @param max max value
	 * @return random value between min and max
	 */
	protected double random(double min, double max) {

		double amt = Math.random();
		return min + (max-min) * amt;

	}

	/**
	 * Get a random long between min and max, inclusive
	 *
	 * @param min min value
	 * @param max max value
	 * @return random value between min and max
	 */
	protected long randomLong(double min, double max) {

		return Math.round(random(min, max));

	}

	/**
	 * Casts a random long as an int
	 *
	 * @param min min value
	 * @param max max value
	 * @return random int between min and max
	 */
	protected int randomInt(double min, double max) {

		return (int)Math.round(random(min, max));

	}

	/**
	 * Return a value around the average, within +- %variance off of average
	 *
	 * @param average the average value
	 * @param variance the variance range from the average
	 * @return a random value within the given bounds
	 */
	protected long randomLongWithinVariance(long average, double variance) {

		return randomLong((double)average * (1.0-variance),
						  (double)average * (1.0+variance));

    }

	/**
	 * Same as randomLongWithinVariance, cast as an int
	 */
	protected int randomIntWithinVariance(int average, double variance) {

		return (int)randomLongWithinVariance(average, variance);

	}


	protected <T> T random(T[] array, double[] probabilities) {

		//replace REMAINDER if there
		for (int x=0; x<probabilities.length; x++) {
			if (probabilities[x] == REMAINDER) {

				//sum the others
				double others = 0;
				for (int y=0; y<probabilities.length; y++) {
					if (y != x) {
						others += probabilities[y];
					}
				}
				double remainder = 1.0 - others;
				probabilities[x] = remainder;
				break; //there can only be one REMAINDER

			}
		}

		double value = Math.random();
		int x = 0;
		double cur = probabilities[0];
		while (value > cur) {
			x++;
			cur += probabilities[x];
		}
		return array[x];

	}

	protected <T> T random(T[] array) {
		int ret = (int)Math.floor(array.length * Math.random());
		return array[ret];
	}

}
