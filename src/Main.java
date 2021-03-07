
import java.io.File;
import java.math.BigInteger;
import java.util.*;

public class Main {

    private static int base;

    private static final int GLYPHO_LEN=4;

    private static final int ERROR=-1;

    private static final int CORRECT=0;

    private static final int EXCEPTION=-2;

    private static ArrayList<Integer> opcodes;

    private static Deque<BigInteger> globalStack;

    private static int nrInstrctions;

    private static Scanner inReader;

    private static Queue<BigInteger> bigIntegers;

    public static final int NOP = 0;
    public static final int INPUT = 1;
    public static final int ROT = 4;
    public static final int SWAP = 5;
    public static final int PUSH = 6;
    public static final int RROT = 16;
    public static final int DUP = 17;
    public static final int ADD = 18;
    public static final int LBRACE = 20;
    public static final int OUT = 21;
    public static final int MUL = 22;
    public static final int EXE = 24;
    public static final int NEG = 25;
    public static final int POP = 26;
    public static final int RBRACE = 27;

    private static HashMap<Integer,Integer> cycles;


    public static void main(String[] args) throws Exception
    {
        String rawData = readRawData(args[0]);
        if(args.length>1)
            base=Integer.parseInt(args[1]);
        else
            base=10;


        inReader = new Scanner(System.in);

        opcodes = new ArrayList<>();

        cycles = new HashMap<>();

        globalStack = new ArrayDeque<>();

        bigIntegers = new LinkedList<>();

        readBigIntegers();

        buildCode(rawData);



        checkBraces();

        executeCode();

        System.exit(CORRECT);


    }

    private static void readBigIntegers()
    {
        while (inReader.hasNextBigInteger(base))
            bigIntegers.add(inReader.nextBigInteger(base));
    }

    private static String readRawData(String fileName)
    {
        try{
            Scanner reader = new Scanner(new File(fileName));
            return reader.next();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;

    }

    private static void buildCode(String rawData)
    {
         nrInstrctions = rawData.length()/GLYPHO_LEN;
        if(rawData.length()%GLYPHO_LEN!=0)
            printError(nrInstrctions);



        for(int i=0;i<rawData.length();i=i+GLYPHO_LEN)
        {
            opcodes.add(decrypt(rawData.substring(i,i+GLYPHO_LEN)));
        }


    }

    private static int decrypt(String command)
    {
        ArrayList<Character> visited = new ArrayList<>();
        visited.add(command.charAt(0));
        int opcode=0;
        boolean flag;
        for(int i=1;i<GLYPHO_LEN;i++) {
            flag=false;
            for (int j = 0; j < visited.size(); j++)
                if (visited.get(j) == command.charAt(i))
                {
                    flag=true;
                    opcode+=j<<2*(GLYPHO_LEN-i-1);
                    break;
                }
            if(flag==false)
            {
                opcode+=visited.size()<<2*(GLYPHO_LEN-i-1);
                visited.add(command.charAt(i));
            }
        }

        return opcode;
    }

    private static int decrypt(ArrayList<BigInteger> bigs)
    {
        ArrayList<BigInteger> visited = new ArrayList<>();
        visited.add(bigs.get(0));

        int opcode=0;
        boolean flag;

        for(int i=1;i<GLYPHO_LEN;i++)
        {
            flag=false;
            for(int j=0;j<visited.size();j++)
                if(visited.get(j).equals(bigs.get(i)))
                {
                    flag=true;
                    opcode+=j<<2*(GLYPHO_LEN-i-1);
                    break;
                }
            if(flag==false)
            {
                opcode+=visited.size()<<2*(GLYPHO_LEN-i-1);
                visited.add(bigs.get(i));
            }
        }

        return opcode;
    }

    private static void printError(int index)
    {
        System.err.println("Error:"+index);

        System.exit(ERROR);
    }

    private static void printException(int index)
    {
        System.err.println("Exception:"+index);

        System.exit(EXCEPTION);
    }

    private static void checkBraces()
    {
        Stack<Integer> leftIdx = new Stack<>();

        for(int i=0;i< opcodes.size();i++)
        {
            if(opcodes.get(i) == LBRACE)
                leftIdx.push(i);
            else if(opcodes.get(i)==RBRACE){
                if(leftIdx.empty()==true)
                    printError(i);
                else
                {
                    cycles.put(i,leftIdx.peek());
                    leftIdx.pop();
                }

            }

        }

        if(leftIdx.empty()==false)
            printError(leftIdx.peek());
    }

    private static void executeCode()
    {
        int eip=0;

        while (eip<nrInstrctions)
        {
           eip=chooseInstruction(eip,opcodes.get(eip));
        }

    }

    private static int chooseInstruction(int eip,int opcode)
    {


        switch (opcode)
        {

            case NOP:
                eip++;
                break;
            case INPUT:
                globalStack.addFirst(bigIntegers.remove());
                eip++;
                break;
            case ROT:
                if(globalStack.isEmpty())
                    printException(eip);

                globalStack.addLast(globalStack.peekFirst());
                globalStack.removeFirst();
                eip++;
                break;
            case SWAP:
                if(globalStack.size()<2)
                    printException(eip);

                BigInteger a = globalStack.peekFirst();
                globalStack.removeFirst();

                BigInteger b = globalStack.peekFirst();
                globalStack.removeFirst();

                globalStack.addFirst(a);
                globalStack.addFirst(b);

                eip++;
                break;
            case PUSH:
                globalStack.addFirst(new BigInteger("1",base));

                eip++;
                break;
            case RROT:
                if(globalStack.isEmpty())
                    printException(eip);

                globalStack.addFirst(globalStack.peekLast());
                globalStack.removeLast();
                eip++;
                break;
            case DUP:
                if(globalStack.isEmpty())
                    printException(eip);

                globalStack.addFirst(globalStack.peekFirst());
                eip++;
                break;
            case ADD:
                if(globalStack.size()<2)
                    printException(eip);

                BigInteger x = globalStack.peekFirst();
                globalStack.removeFirst();

                BigInteger y = globalStack.peekFirst();
                globalStack.removeFirst();

                x = x.add(y);
                globalStack.addFirst(x);
                eip++;
                break;
            case LBRACE:
                if(globalStack.isEmpty())
                    printException(eip);

                if(globalStack.peekFirst().equals(BigInteger.ZERO))
                {
                    while(opcodes.get(eip)!=RBRACE)
                        eip++;

                    eip++;
                }
                else
                    eip++;
                break;
            case OUT:
                if(globalStack.isEmpty())
                    printException(eip);

                System.out.println(globalStack.peekFirst().toString(base).toUpperCase());
                globalStack.removeFirst();
                eip++;
                break;
            case MUL:
                if(globalStack.size()<2)
                    printException(eip);

                BigInteger q = globalStack.peekFirst();
                globalStack.removeFirst();

                BigInteger w = globalStack.peekFirst();
                globalStack.removeFirst();

                q = q.multiply(w);

                globalStack.addFirst(q);
                eip++;
                break;
            case EXE:
                if(globalStack.size()<GLYPHO_LEN)
                    printException(eip);



                ArrayList<BigInteger> bigs = new ArrayList<>();
                for(int i=0;i<GLYPHO_LEN;i++)
                {
                    bigs.add(0,globalStack.peekFirst());
                    globalStack.removeFirst();
                }

                int newOpcode = decrypt(bigs);




                if(newOpcode!=LBRACE && newOpcode!=RBRACE)
                    eip=chooseInstruction(eip,newOpcode);
                else
                    printException(eip);
                break;
            case NEG:
                if(globalStack.isEmpty())
                    printException(eip);

                BigInteger e = globalStack.peekFirst();
                globalStack.removeFirst();

                e=e.negate();
                globalStack.addFirst(e);
                eip++;
                break;
            case POP:
                if(globalStack.isEmpty())
                    printException(eip);

                globalStack.removeFirst();
                eip++;
                break;
            case RBRACE:
                eip=cycles.get(eip);
                break;


        }

        return eip;
    }
}
