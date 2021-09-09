package com.levin.commons.plugins.simple;

import com.levin.commons.plugins.BaseMojo;
import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public abstract class AbstractSshMojo extends BaseMojo {

    public static final int WAIT_CONDITIONS = ChannelCondition.TIMEOUT | ChannelCondition.EOF | ChannelCondition.CLOSED | ChannelCondition.EXIT_STATUS | ChannelCondition.EXIT_SIGNAL;


    @Parameter(defaultValue = "${ssh.host}")
    protected String host;

    @Parameter(defaultValue = "${ssh.port}")
    protected int port = 22;

    @Parameter(defaultValue = "${ssh.username}")
    protected String username = "root";

    @Parameter(defaultValue = "${ssh.password}")
    protected String password;

    @Parameter
    protected String pemPrivateKey;


    protected transient Connection currConnection;

    @Override
    public void executeMojo() throws MojoExecutionException, MojoFailureException {

        if (!hasContent(host)) {
            getLog().warn(getBaseInfo() + " param [host] not set.");
            return;
        }

        checkParams();

        getLog().info(getBaseInfo() + " newSshConnection to " + username + "/" + password + "@" + host + " ...");

        try {
            //读取密码：
            if (password == null) {
                String prompt = "Please input " + username + "@" + host + " 's password:";
                getLog().info(prompt);
                System.out.print(prompt);
                System.err.print(prompt);
                password = new BufferedReader(new InputStreamReader(System.in)).readLine();
                //getLog().info("Password<<<" + password + ">>>");
            }

            if (password == null) {
                password = "";
            }

            currConnection = newConnection();

            getLog().info(getBaseInfo() + " newSshConnection to " + host + " ok.");

            execute(currConnection);

        } catch (Exception e) {
            if (e instanceof MojoExecutionException)
                throw (MojoExecutionException) e;
            else if (e instanceof MojoFailureException)
                throw (MojoFailureException) e;
            else
                throw new MojoExecutionException("execute error", e);
        } finally {
            if (currConnection != null) {
                try {
                    currConnection.close();
                } catch (Exception e) {
                    getLog().warn("close ssh currConnection error", e);
                }
            }
        }
    }


    protected void checkParams() throws MojoFailureException {

        if (port < 1)
            throw new MojoFailureException("port must be > 0");

        if (!hasContent(username))
            throw new MojoFailureException("user no config");

    }

    protected abstract void execute(Connection connection) throws Exception;

    protected Connection newConnection() throws IOException {

        Connection conn = new Connection(host, port);

        conn.connect();

        if (hasContent(password)) {
            if (hasContent(pemPrivateKey))
                conn.authenticateWithPublicKey(username, pemPrivateKey.toCharArray(), password);
            else
                conn.authenticateWithPassword(username, password);
        } else
            conn.authenticateWithNone(username);

        conn.getConnectionInfo();

        conn.sendIgnorePacket();

        return conn;
    }


}
