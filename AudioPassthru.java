import javax.sound.sampled.*;
import java.util.*;
class AudioPassthru {
  static Line selectLine(DataLine.Info lineInfo, String format) throws Exception{
    boolean isSource = lineInfo.getLineClass() == SourceDataLine.class;
    String iotype = isSource ? "output" : "input";
    Mixer.Info[] infos = AudioSystem.getMixerInfo();
    ArrayList<Mixer> mixers = new ArrayList<Mixer>();
    if(format == null)System.out.println("\nSelect "+iotype+" device");
    for(Mixer.Info mi: infos){
      Mixer m = AudioSystem.getMixer(mi);
      if(m.getSourceLineInfo(lineInfo).length==0&&m.getTargetLineInfo(lineInfo).length==0)continue;
      if(format==null){
        System.out.println(mixers.size()+" "+mi.getName());
      }else if(nameMatch(mi.getName(),format)){
        System.out.println(iotype+": "+mi.getName());
        return m.getLine(lineInfo);
      }
      mixers.add(m);
    }
    if(format!=null){
      System.err.println("\u001B[1mError: "+iotype+" device '"+format+"' not found\u001B[m");
      System.err.println("Available devices are:");
      for(Mixer m: mixers)System.out.println(m.getMixerInfo().getName());
      System.exit(-1);
    }
    if(mixers.isEmpty()){
      System.err.println("\u001B[1mError: no "+iotype+" device\u001B[m");
      System.exit(-1);
    }
    while(true){
      System.out.print("> ");
      int index = -1;
      try{index = new Scanner(System.in).nextInt();}catch(Exception e){}
      if(0<=index&&index<mixers.size()){
        Mixer m = mixers.get(index);
        System.out.println(m.getMixerInfo().getName());
        System.out.println();
        return m.getLine(lineInfo);
      }
    }
  }
  static boolean nameMatch(String name, String pattern){
    for(String s: pattern.toLowerCase().split(" ")){
      if(name.toLowerCase().indexOf(s)==-1)return false;
    }
    return true;
  }
  static String argOption(String[] args, String key){
    for(int i=0;i<args.length-1;i++){
      if(args[i].equals(key))return args[i+1];
    }
    return null;
  }
  public static void main(String args[]) throws Exception{
    AudioFormat format=new AudioFormat(44100,16,2,true,false);
    DataLine.Info targetinfo=new DataLine.Info(TargetDataLine.class, format);
    DataLine.Info sourceinfo=new DataLine.Info(SourceDataLine.class, format);
    TargetDataLine input = (TargetDataLine)selectLine(targetinfo, argOption(args, "-i"));
    SourceDataLine output = (SourceDataLine)selectLine(sourceinfo, argOption(args, "-o"));
    input.open(format);
    output.open(format);
    input.start();
    output.start();
    System.out.println("now passing through");
    AudioInputStream in=new AudioInputStream(input);
    byte[] data=new byte[256];
    while(true){
      int a=in.read(data,0,data.length);
      output.write(data,0,data.length);
    }
  }
}
