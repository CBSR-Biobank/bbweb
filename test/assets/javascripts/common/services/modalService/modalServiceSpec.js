/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('modalService', function() {

    var modalElementFind = function() {
      return this.$document.find('body > div.modal');
    };

    var open = function (modalOpenFunc, modalDefaults, modalOptions) {
      var modalElement;
      modalOpenFunc(modalDefaults, modalOptions);
      this.$rootScope.$digest();

      modalElement = modalElementFind.call(this);

      return {
        element: modalElement,
        scope:   modalElement.scope()
      };
    };

    var flush = function () {
      this.$animate.flush();
      this.$rootScope.$digest();
      this.$animate.flush();
      this.$rootScope.$digest();
    };

    var dismiss = function (scope, noFlush) {
      scope.modalOptions.close();
      this.$rootScope.$digest();
      if (!noFlush) {
        flush.call(this);
      }
      return closed;
    };

    beforeEach(mocks.module('ngAnimateMock', 'biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, factory, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$animate',
                              '$document',
                              'modalService',
                              '$filter',
                              'factory');
      self.putHtmlTemplates(
        '/assets/javascripts/common/services/modalService/modal.html',
        '/assets/javascripts/common/services/modalService/modalOk.html');

      testUtils.jasmineAddModalMatchers();
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
        var modalInfo;
        modalInfo = open.call(this, this.modalService.show, undefined);
        expect(this.$document).toHaveModalsOpen(1);
        dismiss.call(this, modalInfo.scope);
      });

    });

    function showSharedBehaviour(context) {

      describe('shared behaviour', function() {

        it('has valid scope', function() {
          var modalInfo = open.call(this, context.modalOpenFunc);
          expect(modalInfo.scope).toBeObject();
          expect(modalInfo.scope.modalOptions).toBeObject();
          expect(modalInfo.scope.modalOptions.ok).toBeFunction();
          expect(modalInfo.scope.modalOptions.close).toBeFunction();
          dismiss.call(this, modalInfo.scope);
        });

        it('can open a modal and close it with the OK button', function() {
          var modalInfo = open.call(this, context.modalOpenFunc);
          expect(this.$document).toHaveModalsOpen(1);
          modalInfo.scope.modalOptions.ok('result');
          this.$rootScope.$digest();
          flush.call(this);
          expect(this.$document).toHaveModalsOpen(0);
        });

        it('can open a modal and close it with the CLOSE button', function() {
          var modalInfo = open.call(this, context.modalOpenFunc);
          expect(this.$document).toHaveModalsOpen(1);
          dismiss.call(this, modalInfo.scope);
          expect(this.$document).toHaveModalsOpen(0);
        });

      });
    }

    describe('modalOk', function() {

      var open = function (headerHtml, bodyHtml) {
        var modalElement;
        this.modalService.modalOk(headerHtml, bodyHtml);
        this.$rootScope.$digest();

        modalElement = modalElementFind.call(this);

        return {
          element: modalElement,
          scope:   modalElement.scope()
        };
      };

      it('can open a modal and close it with the OK button', function() {
        var modalInfo,
            header = this.factory.stringNext(),
            body = this.factory.stringNext();

        modalInfo = open.call(this, header, body);
        expect(this.$document).toHaveModalsOpen(1);
        modalInfo.scope.modalOptions.ok('result');
        this.$rootScope.$digest();
        flush.call(this);
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('has valid header and body', function() {
        var modalInfo,
            header = this.factory.stringNext(),
            body = this.factory.stringNext();

        modalInfo = open.call(this, header, body);
        expect(this.$document).toHaveModalsOpen(1);
        modalInfo.scope.modalOptions.ok('result');
        this.$rootScope.$digest();
        expect(modalInfo.element).toHaveTitle(header);
        expect(modalInfo.element).toHaveBody(body);

        flush.call(this);
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('modalOkCancel', function() {

      var open = function (headerHtml, bodyHtml) {
        var modalElement;
        this.modalService.modalOkCancel(headerHtml, bodyHtml);
        this.$rootScope.$digest();

        modalElement = modalElementFind.call(this);

        return {
          element: modalElement,
          scope:   modalElement.scope()
        };
      };

      it('can open a modal and close it with the OK button', function() {
        var modalInfo,
            header = this.factory.stringNext(),
            body = this.factory.stringNext();

        modalInfo = open.call(this, header, body);
        expect(this.$document).toHaveModalsOpen(1);
        modalInfo.scope.modalOptions.ok('result');
        this.$rootScope.$digest();
        flush.call(this);
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('can open a modal and close it with the CANCEL button', function() {
        var modalInfo,
            header = this.factory.stringNext(),
            body = this.factory.stringNext();

        modalInfo = open.call(this, header, body);
        expect(this.$document).toHaveModalsOpen(1);
        modalInfo.scope.modalOptions.close('result');
        this.$rootScope.$digest();
        flush.call(this);
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('has valid header and body', function() {
        var modalInfo,
            header = this.factory.stringNext(),
            body = this.factory.stringNext();

        modalInfo = open.call(this, header, body);
        expect(this.$document).toHaveModalsOpen(1);
        modalInfo.scope.modalOptions.ok('result');
        this.$rootScope.$digest();
        expect(modalInfo.element).toHaveTitle(header);
        expect(modalInfo.element).toHaveBody(body);
        flush.call(this);
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

  });

});
