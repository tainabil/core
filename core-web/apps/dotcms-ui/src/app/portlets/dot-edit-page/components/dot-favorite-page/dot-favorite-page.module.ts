import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';

import { DotFormDialogModule } from '@components/dot-form-dialog/dot-form-dialog.module';
import { DotTempFileUploadService } from '@dotcms/app/api/services/dot-temp-file-upload/dot-temp-file-upload.service';
import { DotPagesFavoritePageEmptySkeletonComponent } from '@dotcms/app/portlets/dot-pages/dot-pages-favorite-page-empty-skeleton/dot-pages-favorite-page-empty-skeleton.component';
import { DotWorkflowActionsFireService } from '@dotcms/data-access';
import {
    DotAutofocusDirective,
    DotFieldRequiredDirective,
    DotFieldValidationMessageComponent,
    DotMessagePipe
} from '@dotcms/ui';
import { DotPipesModule } from '@pipes/dot-pipes.module';

import { DotFavoritePageComponent } from './dot-favorite-page.component';

@NgModule({
    declarations: [DotFavoritePageComponent],
    exports: [DotFavoritePageComponent],
    imports: [
        CommonModule,
        ButtonModule,
        DotAutofocusDirective,
        DotFormDialogModule,
        DotFieldValidationMessageComponent,
        DotPagesFavoritePageEmptySkeletonComponent,
        DotPipesModule,
        InputTextModule,
        ReactiveFormsModule,
        DotFieldRequiredDirective,
        DotMessagePipe
    ],
    providers: [DotTempFileUploadService, DotWorkflowActionsFireService]
})
export class DotFavoritePageModule {}
