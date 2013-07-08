package org.isatools.isa2owl.converter;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 08/04/2013
 * Time: 16:35
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ISA2OWLCommandLine {

    public static boolean noArguments;

    public static void parseArgs(String[] args){
        noArguments = false;

        int i = 0;
        String arg = null, option;
        while (i < args.length && args[i].startsWith("--")) {
            option = args[i++];

            if (i<args.length)
                arg = args[i++];

            option.toLowerCase();

            if (option.equals("--help")){
                System.out.println("usage: isa2owl.jar [--mode] [--configDir <path>] [--username <username>] [--password <password>] [--isatabDir <path>]\n" +
                        "\t[--isatabFiles <files>] [--help]\n");
                System.out.println("\t--mode\tIndicates ISAcreator mode, the options are NORMAL, LIGHT or GS");
                System.out.println("\t--configDir\tIt sets the path of the directory containing the configuration files");
                System.out.println("\t--username\tIt sets the username for ISAcreator");
                System.out.println("\t--password\tThe password for the username set by --username can be passed to ISAcreator");
                System.out.println("\t--isatabDir\tIt sets the directory containing the ISAtab files");
                System.out.println("\t--isatabFiles <files>\t<files> must be a comma separated list of ISAtab files; this option is only valid for mode GS ");
                System.out.println("\t--help\tShows this message ");
                System.exit(0);


            }
        }
    }


    public static void main(String[] args){
    }
 }
