/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'lodash', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: domainNotificationService', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);
      this.injectDependencies('$q',
                              '$rootScope',
                              'gettext',
                              'domainNotificationService',
                              'modalService');
    }));

    describe('updateErrorModal', function () {

      beforeEach(inject(function () {
        spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      }));

      it('opens a modal when error is a version mismatch error', function() {
        var err = { data: { message: 'expected version doesn\'t match current version' } };
        var domainEntityName = 'entity';
        this.domainNotificationService.updateErrorModal(err, domainEntityName);
        expect(this.modalService.showModal).toHaveBeenCalledWith({
          templateUrl: '/assets/javascripts/common/modalConcurrencyError.html'
        }, {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK',
          domainType: domainEntityName
        });
      });

      it('opens a modal when error is a string', function() {
        var self = this,
            err = { data: { message: 'update error' } };
        self.domainNotificationService.updateErrorModal(err, 'entity');
        expect(self.modalService.showModal).toHaveBeenCalled();
      });

      it('opens a modal when error is a list', function() {
        var self = this,
            err = { data: { message: [ 'update error1', 'update error2' ] } };
        self.domainNotificationService.updateErrorModal(err, 'entity');
        expect(self.modalService.showModal).toHaveBeenCalled();
      });

    });

    describe('removeEntity', function () {

      beforeEach(inject(function () {
        this.remove = jasmine.createSpy('remove').and.returnValue(this.$q.when(true));
      }));

      it('remove works when user confirms the removal', function(done) {
        var header = 'header',
            body = 'body',
            removeFailedHeader = 'removeFailedHeaderHtml',
            removeFailedBody = 'removeFailedBody';

        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));

        this.domainNotificationService.removeEntity(this.remove,
                                                    header,
                                                    body,
                                                    removeFailedHeader,
                                                    removeFailedBody)
          .then(function () { done(); });
        this.$rootScope.$digest();
        expect(this.remove).toHaveBeenCalled();
      });

      it('works when user cancels the removal', function() {
        var header = 'header',
            body = 'body',
            removeFailedHeader = 'removeFailedHeaderHtml',
            removeFailedBody = 'removeFailedBody',
            deferred = this.$q.defer();

        spyOn(this.modalService, 'modalOkCancel').and.returnValue(deferred.promise);
        deferred.reject('simulated error');

        this.domainNotificationService.removeEntity(this.remove,
                                              header,
                                              body,
                                              removeFailedHeader,
                                              removeFailedBody);

        this.$rootScope.$digest();
        expect(this.remove).not.toHaveBeenCalled();
      });

      it('displays the removal failed modal', function() {
        var self = this,
            header = 'header',
            body = 'body',
            removeFailedHeader = 'removeFailedHeaderHtml',
            removeFailedBody = 'removeFailedBody',
            deferred = self.$q.defer();

        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        this.remove = jasmine.createSpy('remove').and.returnValue(deferred.promise);
        deferred.reject('simulated error');

        this.domainNotificationService.removeEntity(this.remove,
                                         header,
                                         body,
                                         removeFailedHeader,
                                         removeFailedBody);
        this.$rootScope.$digest();
        expect(this.remove).toHaveBeenCalled();
        expect(this.modalService.modalOkCancel.calls.count()).toEqual(2);
      });

    });

  });

});
