## Audio Passthru
loopback device -> headphone

microphone -> speaker

### How to use
```
$ java AudioPassthru
Select input device
0 Default Audio Device
1 Built-in Microphone
2 Soundflower (2ch)
3 Soundflower (64ch)
> 2
Soundflower (2ch)

Select output device
0 Default Audio Device
1 Built-in Output
2 Soundflower (2ch)
3 Soundflower (64ch)
> 1
Built-in Output

now passing through...
```

```
$ java AudioPassthru -i "flower 2ch" -o built
input: Soundflower (2ch)
output: Built-in Output
now passing through...
```

### Compile
```
$ javac AudioPassthru.java
```

### Shortcut
```
alias audiopassthru="java -cp PATH_TO_CLASSFILE_DIR AudioPassthru"
```
