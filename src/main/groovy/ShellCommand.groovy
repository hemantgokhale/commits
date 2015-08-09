class ShellCommand {
    static String run(String command, String workingDir) {
        // println command
        Process process = command.execute(null, workingDir?.asType(File))
        def out = new StringBuffer()
        def err = new StringBuffer()
        process.consumeProcessOutput(out, err)
        if (process.waitFor()) {
            println "Error while running command: $command"
            throw new RuntimeException(err.toString())
        }
        out.toString()
    }
}
