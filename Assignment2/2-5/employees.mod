set DAYS;
set ROSTERS;

param employees_needed {DAYS} >= 0 ;
param workdays {DAYS,ROSTERS} binary;

var Employees {ROSTERS} >= 0 ;

minimize Total_Employees :
    sum {r in ROSTERS} Employees[r];

subject to Employees_Needed {d in DAYS} :
    sum {r in ROSTERS} workdays[d,r] * Employees[r] >= employees_needed[d];
