
   Filename hap dde  'Excel|J:\VET\Teladorsagia\Texelmjs\JOanalyses\[Texel_Haplotypes.xls]DQB2!R2C1:R353C3';

data haplo;
    infile hap missover;
	input lamb $ shap $ dhap $;
if shap = '1' then shap = '01';
if shap = '2' then shap = '02';
if shap = '3' then shap = '03';
if shap = '4' then shap = '04';
if shap = '5' then shap = '05';
if shap = '6' then shap = '06';
if shap = '7' then shap = '07';
if shap = '8' then shap = '08';
if shap = '9' then shap = '09';
if dhap = '1' then dhap = '01';
if dhap = '2' then dhap = '02';
if dhap = '3' then dhap = '03';
if dhap = '4' then dhap = '04';
if dhap = '5' then dhap = '05';
if dhap = '6' then dhap = '06';
if dhap = '7' then dhap = '07';
if dhap = '8' then dhap = '08';
if dhap = '9' then dhap = '09';
if shap = ' ' then if dhap = ' ' then delete;
   run;

proc sort;
 by lamb;
 run;

proc print data=haplo;
 run;
/*
 proc export 
 data = haplo outfile = 'C:\\Users\\mjs1z\\Documents\\Texelhaplotypes.txt' REPLACE;
 run;
*/
 Filename jjp dde 
'Excel|J:\Research Groups\Teladorsagia\Texelmjs\JOanalyses\[Texeligaigejjp.xls]sheet1!R10C3:R231C7';

data jjpab;
 infile jjp missover;
 input lamb	$ igel3 igel4 igal3 igal4;
 if igel3 < 0 then igel3 = 0;
 if igel4 < 0 then igel4 = 0;
 if igal3 < 0 then igal3 = 0;
 if igal4 < 0 then igal4 = 0;
 ligel3 = log(igel3+1);
 ligel4 = log(igel4+1);
 ligal3 = log(igal3+1);
 ligal4 = log(igal4+1);
 igel3g=igel3+0.001;
 igel4g=igel4+0.001;
 igal3g=igal3+0.001;
 igal4g=igal4+0.001;
 tigel3 = (igel3g)**0.25;
 tigel4 = (igel4g)**0.25;
 ligal3=log10(igal3g);
 ligal4=log10(igal4g);
 if lamb = '098t062A' then delete;
 if lamb = '098t062B' then delete;

 keep lamb igel3g tigel3;
 run;

proc sort;
 by lamb;
 run;
/*
proc export 
 data = jjpab outfile = 'C:\\Users\\mjs1z\\Documents\\TexelIgE.txt' REPLACE;
 run;
*/

 Filename tweight dde 
'Excel|J:\Research Groups\Teladorsagia\Texelmjs\JOanalyses\[BBtexallrevmjs.xls]sheet1!R2C1:R422C25';

data weight;
 infile tweight missover;
 input lamb	$ sire $ dam $ dnum sgroup dgroup littern week year sex dob dage litters w20 muscle fat 
  stjul nmjul staug nmaug stsep nmsep wgt16 wgt20 wgt24;
  keep lamb sire dam year sex dob;

proc sort;
   by lamb;
   run;


 data all;
 merge haplo jjpab weight;
 by lamb;
 if shap = '?' then delete;
 if dhap = '?' then delete;
 if shap = '10' then shap = '09';
 if dhap = '10' then dhap = '09';
 if shap = '11a' then shap = '11';
 if dhap = '11a' then dhap = '11';
 if shap = '11b' then shap = '11';
 if dhap = '11b' then dhap = '11';
 if shap = '20' then shap = '06';
 if dhap = '20' then dhap = '06';

a=0;
if shap='01' then a=1;
if dhap='01' then a=a+1;
b=0;
if shap='02' then b=1;
if dhap='02' then b=b+1;
c=0;
if shap='03' then c=1;
if dhap='03' then c=c+1;
d=0;
if shap='04' then d=1;
if dhap='04' then d=d+1;
e=0;
if shap='05' then e=1;
if dhap='05' then e=e+1;
f=0;
if shap='06' then f=1;
if dhap='06' then f=f+1;
g=0;
if shap='07' then g=1;
if dhap='07' then g=g+1;
h=0;
if shap='08' then h=1;
if dhap='08' then h=h+1;
i=0;
if shap='09' then i=1;
if dhap='09' then i=i+1;
j=0;
if shap='11' then j=1;
if dhap='11' then j=j+1;
k=0;
if shap='12' then k=1;
if dhap='12' then k=k+1;
l=0;
if shap='13' then l=1;
if dhap='13' then l=l+1;
m=0;
if shap='14' then m=1;
if dhap='14' then m=m+1;
n=0;
if shap='15' then n=1;
if dhap='15' then n=n+1;
o=0;
if shap='16' then o=1;
if dhap='16' then o=o+1;
p=0;
if shap='17' then p=1;
if dhap='17' then p=p+1;
q=0;
if shap='18' then q=1;
if dhap='18' then q=q+1;
r=0;
if shap='19' then r=1;
if dhap='19' then r=r+1;
o=0;
*** set common allele to zero;
***hom = 0;
***if drb1a=drb1b then hom = 1;
run;
proc allele data=all outstat=ld prefix=Marker haplo=given corrcoeff
         hmin = 0.01 gmin = 0.5 exact = 10000 boot=1000 seed=123;
	 var shap dhap;
	 run;

ods trace on;
ods output SolutionF = SF;
***Four alleles not used because of rarity: 5 6 8 19;
*** Four others borderline 3 7 14 17;
proc mixed data=all ;
 class lamb dam sire year; 
 model tigel3 = year sex dob a b d i j k l n q  / solution;
 random sire dam(sire);
*** parms (0.12) (0.04) (0.001);
run;

ods trace off;
ods _all_ close;
ods listing;

data graph1;
 set SF;
 if effect = 'Intercept' then delete;
 if effect = 'year' then delete;
 if effect = 'dob' then delete;
 if effect = 'sex' then delete;
 
proc print data=graph1;
 run;
 
axis1 label = ('IgE L3' font='Times New Roman Symbol') minor = none ;
axis2 value = (angle = 0) label = ('ALLELE' font='Times New Roman Symbol');

proc gchart data = graph1;
 vbar Effect / sumvar = estimate raxis = axis1 descending maxis = axis2;
 run;
 
