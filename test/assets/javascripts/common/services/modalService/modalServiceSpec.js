/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  function SuiteMixinFactory(ModalTestSuiteMixin) {

    function SuiteMixin() {
      ModalTestSuiteMixin.call(this);
    }

    SuiteMixin.prototype = Object.create(ModalTestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    function openCommon() {
      /*jshint validthis:true */
      this.$rootScope.$digest();
      this.modalElement = this.modalElementFind();
      this.scope = this.modalElement.scope();
    }

    SuiteMixin.prototype.openModal = function (modalOpenFunc, modalDefaults, modalOptions) {
      modalOpenFunc(modalDefaults, modalOptions).then(function () {}, function () {});
      openCommon.call(this);
    };

    SuiteMixin.prototype.openModalOk = function (headerHtml, bodyHtml) {
      this.modalService.modalOk(headerHtml, bodyHtml).then(function () {}, function () {});
      openCommon.call(this);
    };

    SuiteMixin.prototype.openModalOkCancel = function (headerHtml, bodyHtml) {
      this.modalService.modalOkCancel(headerHtml, bodyHtml).then(function () {}, function () {});
      openCommon.call(this);
    };

    SuiteMixin.prototype.dismiss = function (scope, noFlush) {
      scope = scope || this.scope;
      scope.modalOptions.close();
      this.$rootScope.$digest();
      if (!noFlush) {
        this.flush();
      }
      return closed;
    };

    return SuiteMixin;
  }

  describe('modalService', function() {

    beforeEach(mocks.module('ngAnimateMock', 'biobankApp', 'biobank.test'));

    beforeEach(inject(function(ModalTestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(ModalTestSuiteMixin);

      _.extend(this, SuiteMixin.prototype);

      this.injectDependencies('$rootScope',
                              '$animate',
                              '$document',
                              'modalService',
                              '$filter',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/common/services/modalService/modal.html',
        '/assets/javascripts/common/services/modalService/modalOk.html');

      this.addModalMatchers();
    }));

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
            header = this.factory.stringNext(),
            body = this.factory.stringNext();

        modalInfo = this.openModalOk(header, body);
        expect(this.$document).toHaveModalsOpen(1);
        this.scope.modalOptions.ok('result');
        this.$rootScope.$digest();
        this.flush();
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('has valid header and body', function() {
        var modalInfo,                                // eslint-disable-line no-unused-vars
            header = this.factory.stringNext(),
            body = this.factory.stringNext();

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
            header = this.factory.stringNext(),
            body = this.factory.stringNext();

        modalInfo = this.openModalOkCancel(header, body);
        expect(this.$document).toHaveModalsOpen(1);
        this.scope.modalOptions.ok('result');
        this.$rootScope.$digest();
        this.flush();
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('can open a modal and close it with the CANCEL button', function() {
        var modalInfo,                                // eslint-disable-line no-unused-vars
            header = this.factory.stringNext(),
            body = this.factory.stringNext();

        modalInfo = this.openModalOkCancel(header, body);
        expect(this.$document).toHaveModalsOpen(1);
        this.scope.modalOptions.close('result');
        this.$rootScope.$digest();
        this.flush();
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('has valid header and body', function() {
        var modalInfo,                                // eslint-disable-line no-unused-vars
            header = this.factory.stringNext(),
            body = this.factory.stringNext();

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

});
