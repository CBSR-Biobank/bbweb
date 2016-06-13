/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: domainEntityService', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (testSuiteMixin) {
      _.extend(this, testSuiteMixin);
      this.injectDependencies('$q',
                              '$rootScope',
                              'domainEntityService',
                              'modalService');
    }));

    describe('updateErrorModal', function () {

      beforeEach(inject(function () {
        spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      }));

      it('opens a modal when error is a version mismatch error', function() {
        var err = { data: { message: 'expected version doesn\'t match current version' } };
        var domainEntityName = 'entity';
        this.domainEntityService.updateErrorModal(err, domainEntityName);
        expect(this.modalService.showModal).toHaveBeenCalledWith({
          templateUrl: '/assets/javascripts/common/modalConcurrencyError.html'
        }, {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK',
          domainType: domainEntityName
        });
      });

      it('opens a modal when error is a string', function() {
        var err = { data: { message: 'update error' } };
        this.domainEntityService.updateErrorModal(err, 'entity');
        expect(this.modalService.showModal).toHaveBeenCalledWith({}, {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK',
          headerHtml: 'Cannot submit this change',
          bodyHtml: 'Error: ' + err.data.message
        });
      });

      it('opens a modal when error is a list', function() {
        var err = { data: { message: [ 'update error1', 'update error2' ] } };
        this.domainEntityService.updateErrorModal(err, 'entity');
        expect(this.modalService.showModal).toHaveBeenCalledWith({}, {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK',
          headerHtml: 'Cannot submit this change',
          bodyHtml: 'Error: ' + JSON.stringify(err.data.message)
        });
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

        spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));

        this.domainEntityService.removeEntity(this.remove,
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

        spyOn(this.modalService, 'showModal').and.returnValue(deferred.promise);
        deferred.reject('simulated error');

        this.domainEntityService.removeEntity(this.remove,
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

        spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
        this.remove = jasmine.createSpy('remove').and.returnValue(deferred.promise);
        deferred.reject('simulated error');

        this.domainEntityService.removeEntity(this.remove,
                                         header,
                                         body,
                                         removeFailedHeader,
                                         removeFailedBody);
        this.$rootScope.$digest();
        expect(this.remove).toHaveBeenCalled();
        expect(this.modalService.showModal.calls.count()).toEqual(2);
      });

    });

  });

});
