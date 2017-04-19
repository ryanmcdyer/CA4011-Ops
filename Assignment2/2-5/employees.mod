set DAYS;
set ROSTERS;

param workdays {DAYS,ROSTERS} binary;
param staff_needed {DAYS} >= 0 ;

var Employees {ROSTERS} >= 0 ;

minimize Total_Employees :
    sum {r in ROSTERS} Employees[r] ;

subject to Staff_Needed {d in DAYS} :
    sum {r in ROSTERS} workdays[d,r] * Employees[r] >= staff_needed[d] ;
