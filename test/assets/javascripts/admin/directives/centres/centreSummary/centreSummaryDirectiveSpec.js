/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular                = require('angular'),
      mocks                  = require('angularMocks'),
      _                      = require('underscore'),
      entityUpdateSharedSpec = require('../../../../test/entityUpdateSharedSpec');

  describe('Directive: centreSummaryDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, modalService, templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q                   = self.$injector.get('$q');
      self.Centre               = self.$injector.get('Centre');
      self.CentreStatus         = self.$injector.get('CentreStatus');
      self.notificationsService = self.$injector.get('notificationsService');
      self.factory              = self.$injector.get('jsonEntities');

      self.centre = new self.Centre(self.jsonEntities.centre());

      spyOn(modalService, 'showModal').and.callFake(function () {
        return self.$q.when('modalResult');
      });

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/centres/centreSummary/centreSummary.html',
        '/assets/javascripts/common/directives/truncateToggle.html',
        '/assets/javascripts/admin/directives/statusLine/statusLine.html');

      self.createController = createController;

      //--

      function createController(centre) {
        self.element = angular.element('<centre-summary centre="vm.centre"></centre-summary>');
        self.scope = $rootScope.$new();
        self.scope.vm = { centre: self.centre };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('centreSummary');
      }
    }));

    it('should contain valid settings to display the centre summary', function() {
      this.createController(this.centre);
      expect(this.scope.vm.centre).toBe(this.centre);
      expect(this.controller.descriptionToggleLength).toBeDefined();
    });

    describe('change centre status', function() {

      describe('should enable a centre', function() {
        var context = {};

        beforeEach(inject(function () {
          context.centre = this.centre;
          context.status = 'enable';
          context.newStatus = this.CentreStatus.ENABLED;
        }));

        statusChangeSharedBehaviour(context);
      });

      describe('should disable a centre', function() {
        var context = {};

        beforeEach(function () {
          this.centre.status = this.CentreStatus.ENABLED;
          context.centre = this.centre;
          context.status = 'disable';
          context.newStatus = this.CentreStatus.DISABLED;
        });

        statusChangeSharedBehaviour(context);
      });

      it('should throw an error when attempting to change status to invalid state', function() {
        var self = this;

        self.createController(self.centre);
        expect(function () {
          self.controller.changeStatus(self.factory.stringNext());
        }).toThrowError(/invalid status:/);
      });

    });

    describe('updates to name', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity             = this.Centre;
        context.updateFuncName     = 'updateName';
        context.controllerFuncName = 'editName';
        context.modalInputFuncName = 'text';
      }));

      entityUpdateSharedSpec(context);

    });

    describe('updates to description', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity             = this.Centre;
        context.updateFuncName     = 'updateDescription';
        context.controllerFuncName = 'editDescription';
        context.modalInputFuncName = 'textArea';
      }));

      entityUpdateSharedSpec(context);

    });

  });


  function statusChangeSharedBehaviour(context) {

    describe('(shared) centre status', function() {

      it('status is changed', function() {
        var self = this;

        self.createController(context.centre);

        spyOn(self.Centre.prototype, context.status).and.callFake(function () {
          context.centre.status = (context.centre.status === self.CentreStatus.ENABLED) ?
            self.CentreStatus.DISABLED : self.CentreStatus.ENABLED;
          return self.$q.when(context.centre);
        });

        self.controller.changeStatus(context.status);
        self.scope.$digest();
        expect(self.Centre.prototype[context.status]).toHaveBeenCalled();
        expect(self.scope.vm.centre.status).toBe(context.newStatus);
      });
    });

  }

});
