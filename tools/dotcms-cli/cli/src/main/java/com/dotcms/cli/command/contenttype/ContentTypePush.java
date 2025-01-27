package com.dotcms.cli.command.contenttype;

import com.dotcms.api.client.push.PushService;
import com.dotcms.api.client.push.contenttype.ContentTypeComparator;
import com.dotcms.api.client.push.contenttype.ContentTypeFetcher;
import com.dotcms.api.client.push.contenttype.ContentTypePushHandler;
import com.dotcms.cli.command.DotCommand;
import com.dotcms.cli.command.DotPush;
import com.dotcms.cli.common.OutputOptionMixin;
import com.dotcms.cli.common.PushMixin;
import com.dotcms.common.WorkspaceManager;
import com.dotcms.model.config.Workspace;
import com.dotcms.model.push.PushOptions;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import picocli.CommandLine;

@ActivateRequestContext
@CommandLine.Command(
        name = ContentTypePush.NAME,
        header = "@|bold,blue Push content types|@",
        description = {
                "This command enables the pushing of content types to the server. It accommodates the "
                        + "specification of either a content type file or a folder path.",
                "" // empty string to add a new line
        }
)
public class ContentTypePush extends AbstractContentTypeCommand implements Callable<Integer>,
        DotCommand, DotPush {

    static final String NAME = "push";

    static final String CONTENT_TYPE_PUSH_MIXIN = "contentTypePushMixin";

    @CommandLine.Mixin
    PushMixin pushMixin;

    @CommandLine.Mixin(name = CONTENT_TYPE_PUSH_MIXIN)
    ContentTypePushMixin contentTypePushMixin;

    @Inject
    WorkspaceManager workspaceManager;

    @Inject
    PushService pushService;

    @Inject
    ContentTypeFetcher contentTypeProvider;

    @Inject
    ContentTypeComparator contentTypeComparator;

    @Inject
    ContentTypePushHandler contentTypePushHandler;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {

        // When calling from the global push we should avoid the validation of the unmatched
        // arguments as we may send arguments meant for other push subcommands
        if (!pushMixin.noValidateUnmatchedArguments) {
            // Checking for unmatched arguments
            output.throwIfUnmatchedArguments(spec.commandLine());
        }

        // Make sure the path is within a workspace
        final Optional<Workspace> workspace = workspaceManager.findWorkspace(
                this.getPushMixin().path()
        );
        if (workspace.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("No valid workspace found at path: [%s]",
                            this.getPushMixin().path.toPath()));
        }

        File inputFile = this.getPushMixin().path().toFile();
        if (!inputFile.isAbsolute()) {
            inputFile = Path.of(workspace.get().languages().toString(), inputFile.getName())
                    .toFile();
        }
        if (!inputFile.exists() || !inputFile.canRead()) {
            throw new IOException(String.format(
                    "Unable to access the path [%s] check that it does exist and that you have "
                            + "read permissions on it.", inputFile)
            );
        }

        // To make sure that if the user is passing a directory we use the content types folder
        if (inputFile.isDirectory()) {
            inputFile = workspace.get().contentTypes().toFile();
        }

        // Execute the push
        pushService.push(
                inputFile,
                PushOptions.builder().
                        failFast(pushMixin.failFast).
                        allowRemove(contentTypePushMixin.removeContentTypes).
                        maxRetryAttempts(pushMixin.retryAttempts).
                        dryRun(pushMixin.dryRun).
                        build(),
                output,
                contentTypeProvider,
                contentTypeComparator,
                contentTypePushHandler
        );

        return CommandLine.ExitCode.OK;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public OutputOptionMixin getOutput() {
        return output;
    }

    @Override
    public PushMixin getPushMixin() {
        return pushMixin;
    }

    @Override
    public Optional<String> getCustomMixinName() {
        return Optional.of(CONTENT_TYPE_PUSH_MIXIN);
    }

}
