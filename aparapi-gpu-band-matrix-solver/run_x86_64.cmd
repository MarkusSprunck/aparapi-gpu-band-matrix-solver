@ECHO OFF

ECHO  === Delete output files
del run_x86_64_output.txt
del run_x86_64_clinfo.txt

ECHO  === Store Information about GPU
clinfo.exe > run_x86_64_clinfo.txt 

ECHO  === Run all tests start 
java -Dcom.amd.aparapi.enableProfiling=true -classpath aparapi.jar;aparapi-gpu-band-matrix-solver.jar tests.TestRunner > run_x86_64_output.txt
