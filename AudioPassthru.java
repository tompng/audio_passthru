import javax.sound.sampled.*;
import java.util.*;
class AudioPassthru {
  static Line selectLine(DataLine.Info lineInfo, String format) throws Exception{
    boolean isSource = lineInfo.getLineClass() == SourceDataLine.class;
    String iotype = isSource ? "output" : "input";
    Mixer.Info[] infos = AudioSystem.getMixerInfo();
    if(format!=null){
      for(Mixer.Info mi: infos){
        Mixer m = AudioSystem.getMixer(mi);
        if(m.getSourceLineInfo(lineInfo).length==0&&m.getTargetLineInfo(lineInfo).length==0)continue;
        if(nameMatch(mi.getName(),format))return m.getLine(lineInfo);
      }
      throw new Exception(iotype+" device '"+format+"' not found");
    }
    ArrayList<Mixer> mixers = new ArrayList<Mixer>();
    System.out.println("select "+iotype+" device");
    for(Mixer.Info mi: infos){
      Mixer m = AudioSystem.getMixer(mi);
      if(m.getSourceLineInfo(lineInfo).length==0&&m.getTargetLineInfo(lineInfo).length==0)continue;
      System.out.println(mixers.size()+" "+mi.getName());
      mixers.add(m);
    }
    if(mixers.isEmpty()){
      throw new Exception("no "+iotype+" device");
    }
    while(true){
      System.out.print("> ");
      int index = new Scanner(System.in).nextInt();
      if(0<=index&&index<mixers.size()){
        return mixers.get(index).getLine(lineInfo);
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
    AudioInputStream in=new AudioInputStream(input);
    byte[] data=new byte[256];
    while(true){
      int a=in.read(data,0,data.length);
      output.write(data,0,data.length);
    }
  }
}
