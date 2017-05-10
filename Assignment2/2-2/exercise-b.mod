set EXERCISES;

param tolerance {EXERCISES};
param minimum {EXERCISES};
param calories {EXERCISES};
#special exercises are the ones you can only do less than x hours of cumulatively
param special {EXERCISES} binary;
param special_exercise_limit;

var Do {e in EXERCISES} <= tolerance[e], >= minimum[e]; # tons produced

minimize Total_Time_Used: sum {e in EXERCISES} Do[e];
subject to Tolerance {e in EXERCISES}:
    Do[e] <= tolerance[e];
subject to Calories_Burned:
    sum {e in EXERCISES} Do[e] * calories[e] >= 2000;
subject to Limited_Exercises:
    sum {e in EXERCISES} special[e] * Do[e] <= special_exercise_limit;
