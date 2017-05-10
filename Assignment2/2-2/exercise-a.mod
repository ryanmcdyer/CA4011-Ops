set EXERCISES;


param tolerance {EXERCISES};
param calories {EXERCISES};

var Do {e in EXERCISES} <= tolerance[e], >= 0; # tons produced

minimize Total_Time_Used: sum {e in EXERCISES} Do[e];
subject to Tolerance {e in EXERCISES}:
    Do[e] <= tolerance[e];
subject to Calories_Burned:
    sum {E in EXERCISES} Do[e] * calories[e] >= min_calories ;

