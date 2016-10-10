import javax.sound.sampled.*;
import java.util.*;
import java.util.stream.*;
class AudioPassthru {
  static void errorMessage(String message){
    System.err.println("\u001B[1mError: " + message + "\u001B[m");
  }
  static Line selectLine(DataLine.Info lineInfo, String pattern) throws Exception{
    boolean isSource = lineInfo.getLineClass() == SourceDataLine.class;
    String iotype = isSource ? "output" : "input";
    List<Mixer> mixers = Arrays.asList(AudioSystem.getMixerInfo())
                               .stream().map(i -> AudioSystem.getMixer(i))
                               .filter(m -> m.getSourceLineInfo(lineInfo).length + m.getTargetLineInfo(lineInfo).length > 0)
                               .collect(Collectors.toList());
    if(mixers.isEmpty()){
      errorMessage("no " + iotype + " device");
      System.exit(-1);
    }
    if(pattern != null){
      Mixer mixer = null;
      try{
        mixer = mixers.stream().filter(m -> nameMatch(m.getMixerInfo().getName(), pattern)).findFirst().get();
      }catch(NoSuchElementException e){
        errorMessage(iotype + " device '" + pattern + "' not found");
        System.err.println("Available devices:");
        for(Mixer m: mixers)System.out.println(m.getMixerInfo().getName());
        System.exit(-1);
      }
      System.out.println(iotype + ": " + mixer.getMixerInfo().getName());
      return mixer.getLine(lineInfo);
    }
    System.out.println("Select " + iotype + " device");
    for(int i = 0; i < mixers.size(); i++){
      System.out.println(i + " " + mixers.get(i).getMixerInfo().getName());
    }
    while(true){
      System.out.print("> ");
      int index = -1;
      try{index = new Scanner(System.in).nextInt();}catch(InputMismatchException e){}
      if(0 <= index && index < mixers.size()){
        Mixer m = mixers.get(index);
        System.out.println(m.getMixerInfo().getName());
        System.out.println();
        return m.getLine(lineInfo);
      }
    }
  }
  static boolean nameMatch(String name, String pattern){
    for(String s: pattern.toLowerCase().split(" ")){
      if(name.toLowerCase().indexOf(s) == -1)return false;
    }
    return true;
  }
  static String argOption(String[] args, String key){
    for(int i = 0; i < args.length-1; i++){
      if(args[i].equals(key))return args[i+1];
    }
    return null;
  }
  public static void main(String args[]) throws Exception{
    AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
    DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
    DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
    TargetDataLine input = (TargetDataLine)selectLine(targetInfo, argOption(args, "-i"));
    SourceDataLine output = (SourceDataLine)selectLine(sourceInfo, argOption(args, "-o"));
    input.open(format);
    output.open(format);
    input.start();
    output.start();
    System.out.println("now passing through");
    byte[] data=new byte[256];
    while(true){
      input.read(data, 0, data.length);
      output.write(data, 0, data.length);
    }
  }
}
