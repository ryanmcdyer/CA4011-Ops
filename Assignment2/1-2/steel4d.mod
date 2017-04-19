
set PROD; # products
set STAGE; # stages

param rate {PROD,STAGE} > 0; # tons per hour in each stage
param avail {STAGE} >= 0; # hours available/week in each stage
param profit {PROD}; # profit per ton

#param commit {PROD} >= 0; # lower limit on tons sold in week
param share {PROD} >= 0; # new lower limit
param market {PROD} >= 0; # upper limit on tons sold in week

##var Make {p in PROD} >= commit[p], <= market[p]; # tons produced

var Make {p in PROD} <= market[p]; # tons produced


# Objective: total profits from all products
maximize Total_Profit: sum {p in PROD} profit[p] * Make[p];
subject to Time {s in STAGE}:
  sum {p in PROD} (1/rate[p,s]) * Make[p] <= avail[s];


subject to Min_share {p in PROD}:
  Make[p] >= share[p] * sum {p0 in PROD} Make[p0];

# In each stage: total of hours used by all
# products may not exceed hours available
