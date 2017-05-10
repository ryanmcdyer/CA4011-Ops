set PLANTS;#
set MILLS;# origins
set FACTORIES;# destinations

param avail_slab {PLANTS} >= 0;# Slabs available at plants
param supply {MILLS} >= 0;# amounts available at origins
param demand {FACTORIES} >= 0;# amounts required at destinations

param supply_pct;
param demand_pct;

check: sum {p in PLANTS} avail_slab[p] = sum {m in MILLS} supply[m];
check: sum {m in MILLS} supply[m] = sum {f in FACTORIES} demand[f];

param cost_slab {PLANTS,MILLS} >= 0;# shipment costs per unit
var Trans_Slab {PLANTS,MILLS} >= 0;# units to be shipped

param cost {MILLS,FACTORIES} >= 0;# shipment costs per unit
var Trans {MILLS,FACTORIES} >= 0;# units to be shipped

minimize Total_Cost:
    sum {p in PLANTS, m in MILLS} cost_slab[p,m] * Trans_Slab[p,m] +
    sum {m in MILLS, f in FACTORIES} cost[m,f] * Trans[m,f]
    - sum {m in MILLS} scrap[m] * scrap_price[m];


subject to Supply_Slabs {p in PLANTS}:
    sum {m in MILLS} Trans_Slab[p,m] = avail_slab[p];

#also, shipments out of each mill will have to equal shipments in.
subject to In_Out_Equality {m in MILLS}:
    sum {p in PLANTS} Trans_Slab[p,m] = sum {f in FACTORIES} Trans[m,f] ;


subject to Supply {m in MILLS}:
    sum {f in FACTORIES} Trans[m,f] = supply[m];
subject to Demand {f in FACTORIES}:
    sum {m in MILLS} Trans[m,f] = demand[f];



subject to Slab_Supply_Pct {p in PLANTS, m in MILLS}:
    Trans_Slab[p,m] <= avail_slab[p] * supply_pct;

subject to Demand_Supply_Pct {p in PLANTS, m in MILLS}:
    Trans_Slab[p,m] <= supply[m] * demand_pct;
