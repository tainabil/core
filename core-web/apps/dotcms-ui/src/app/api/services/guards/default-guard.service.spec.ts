import { TestBed } from '@angular/core/testing';

import { DotRouterService } from '@dotcms/app/api/services/dot-router/dot-router.service';
import { DOTTestBed } from '@dotcms/app/test/dot-test-bed';

import { DefaultGuardService } from './default-guard.service';

describe('ValidDefaultGuardService', () => {
    let defaultGuardService: DefaultGuardService;
    let dotRouterService: DotRouterService;

    beforeEach(() => {
        DOTTestBed.configureTestingModule({
            providers: [DefaultGuardService]
        });

        defaultGuardService = TestBed.get(DefaultGuardService);
        dotRouterService = TestBed.get(DotRouterService);
    });

    it('should redirect to to Main Portlet always', () => {
        const result = defaultGuardService.canActivate();
        expect(dotRouterService.goToMain).toHaveBeenCalled();
        expect(result).toBe(true);
    });
});
