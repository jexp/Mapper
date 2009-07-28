cd ..
javac -d build src/de/mesirii/mapper/*.java
if [ $? -eq 0 ]; then
cd build
zip -r ../bin/mapper.zip de
cd ..
fi
cd bin

