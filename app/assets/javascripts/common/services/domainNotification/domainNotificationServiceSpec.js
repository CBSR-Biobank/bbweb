/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import ngModule from '../../index'

describe('Service: domainNotificationService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function () {
      Object.assign(this, TestSuiteMixin);
      this.injectDependencies('$q',
                              '$rootScope',
                              'gettext',
                              'domainNotificationService',
                              'modalService');
    });
  });

  describe('updateErrorModal', function () {

    beforeEach(function () {
      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
    });

    it('opens a modal when error is a version mismatch error', function() {
      var domainEntityName = 'entity',
          err = {
            status:  'error',
            message: 'expected version doesn\'t match current version'
          };

      this.domainNotificationService.updateErrorModal(err, domainEntityName);
      expect(this.modalService.showModal).toHaveBeenCalledWith({
        template: require('./modalConcurrencyError.html')
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

    beforeEach(function () {
      this.remove = jasmine.createSpy('remove').and.returnValue(this.$q.when(true));
    });

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
      const header = 'header';
      const body = 'body';
      const removeFailedHeader = 'removeFailedHeaderHtml';
      const removeFailedBody = 'removeFailedBody';

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.reject('simulated error'));
      this.domainNotificationService
        .removeEntity(this.remove, header, body, removeFailedHeader, removeFailedBody)
        .catch(angular.noop);

      this.$rootScope.$digest();
      expect(this.remove).not.toHaveBeenCalled();
    });

    it('displays the removal failed modal', function() {
      const header = 'header';
      const body = 'body';
      const removeFailedHeader = 'removeFailedHeaderHtml';
      const removeFailedBody = 'removeFailedBody';

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      this.remove = jasmine.createSpy('remove').and.returnValue(this.$q.reject('simulated error'));

      this.domainNotificationService
        .removeEntity(this.remove, header, body, removeFailedHeader, removeFailedBody)
        .catch(angular.noop);

      this.$rootScope.$digest();
      expect(this.remove).toHaveBeenCalled();
      expect(this.modalService.modalOkCancel.calls.count()).toEqual(2);
    });

  });

});
