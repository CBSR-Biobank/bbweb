/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('modalService', function() {

  beforeEach(() => {
    angular.mock.module('ngAnimateMock', 'biobankApp', 'biobank.test');
    angular.mock.inject(function(ModalTestSuiteMixin) {
      _.extend(this, ModalTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$animate',
                              '$document',
                              'modalService',
                              '$filter',
                              'Factory');
      function openCommon() {
        /*jshint validthis:true */
        this.$rootScope.$digest();
        this.modalElement = this.modalElementFind();
        this.scope = this.modalElement.scope();
      }

      this.openModal = (modalOpenFunc, modalDefaults, modalOptions) => {
        modalOpenFunc(modalDefaults, modalOptions).then(function () {}, function () {});
        openCommon.call(this);
      };

      this.openModalOk = (headerHtml, bodyHtml) => {
        this.modalService.modalOk(headerHtml, bodyHtml).then(angular.noop, angular.noop);
        openCommon.call(this);
      };

      this.openModalOkCancel = (headerHtml, bodyHtml) => {
        this.modalService.modalOkCancel(headerHtml, bodyHtml).then(angular.noop, angular.noop);
        openCommon.call(this);
      };

      this.dismiss = (scope, noFlush) => {
        scope = scope || this.scope;
        scope.modalOptions.close();
        this.$rootScope.$digest();
        if (!noFlush) {
          this.flush();
        }
      };

      this.addModalMatchers();
    });
  });

  beforeEach(function () {
    this.modalDefaults = {};
    this.modalOptions = {};
  });

  describe('showModal', function() {
    var context = {};

    beforeEach(function() {
      context.modalOpenFunc = this.modalService.showModal;
    });

    showSharedBehaviour(context);

  });

  describe('show', function() {
    var context = {};

    beforeEach(function() {
      context.modalOpenFunc = this.modalService.show;
    });

    showSharedBehaviour(context);

    it('can open modal with no custom modal defaults', function() {
      this.openModal(this.modalService.show, undefined);
      expect(this.$document).toHaveModalsOpen(1);
      this.dismiss();
    });

  });

  function showSharedBehaviour(context) {

    describe('shared behaviour', function() {

      it('has valid scope', function() {
        this.openModal(context.modalOpenFunc);
        expect(this.scope).toBeObject();
        expect(this.scope.modalOptions).toBeObject();
        expect(this.scope.modalOptions.ok).toBeFunction();
        expect(this.scope.modalOptions.close).toBeFunction();
        this.dismiss();
      });

      it('can open a modal and close it with the OK button', function() {
        this.openModal(context.modalOpenFunc);
        expect(this.$document).toHaveModalsOpen(1);
        this.scope.modalOptions.ok('result');
        this.$rootScope.$digest();
        this.flush();
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('can open a modal and close it with the CLOSE button', function() {
        this.openModal(context.modalOpenFunc);
        expect(this.$document).toHaveModalsOpen(1);
        this.dismiss();
        expect(this.$document).toHaveModalsOpen(0);
      });

    });
  }

  describe('modalOk', function() {

    it('can open a modal and close it with the OK button', function() {
      var modalInfo,                                // eslint-disable-line no-unused-vars
          header = this.Factory.stringNext(),
          body = this.Factory.stringNext();

      modalInfo = this.openModalOk(header, body);
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.modalOptions.ok('result');
      this.$rootScope.$digest();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('has valid header and body', function() {
      var modalInfo,                                // eslint-disable-line no-unused-vars
          header = this.Factory.stringNext(),
          body = this.Factory.stringNext();

      modalInfo = this.openModalOk(header, body);
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.modalOptions.ok('result');
      this.$rootScope.$digest();
      expect(this.modalElement).toHaveModalTitle(header);
      expect(this.modalElement).toHaveModalBody(body);

      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('modalOkCancel', function() {

    it('can open a modal and close it with the OK button', function() {
      var modalInfo,                                // eslint-disable-line no-unused-vars
          header = this.Factory.stringNext(),
          body = this.Factory.stringNext();

      modalInfo = this.openModalOkCancel(header, body);
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.modalOptions.ok('result');
      this.$rootScope.$digest();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('can open a modal and close it with the CANCEL button', function() {
      var modalInfo,                                // eslint-disable-line no-unused-vars
          header = this.Factory.stringNext(),
          body = this.Factory.stringNext();

      modalInfo = this.openModalOkCancel(header, body);
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.modalOptions.close('result');
      this.$rootScope.$digest();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('has valid header and body', function() {
      var modalInfo,                                // eslint-disable-line no-unused-vars
          header = this.Factory.stringNext(),
          body = this.Factory.stringNext();

      modalInfo = this.openModalOkCancel(header, body);
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.modalOptions.ok('result');
      this.$rootScope.$digest();
      expect(this.modalElement).toHaveModalTitle(header);
      expect(this.modalElement).toHaveModalBody(body);
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

});
