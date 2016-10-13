/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular                = require('angular'),
      mocks                  = require('angularMocks'),
      _                      = require('lodash'),
      entityUpdateSharedSpec = require('../../../../test/entityUpdateSharedSpec');

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
      TestSuiteMixin.call(this);
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createController = function (centre) {
      centre = centre || this.centre;
      this.element = angular.element('<centre-summary centre="vm.centre"></centre-summary>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { centre: this.centre };

      this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
      this.scope.$on('centre-view', this.eventRxFunc);

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('centreSummary');
    };

    return SuiteMixin;
  }

  describe('Directive: centreSummaryDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(modalService, TestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Centre',
                              'CentreStatus',
                              'notificationsService',
                              'factory');
      this.centre = new this.Centre(this.factory.centre());
      this.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/centreView/centreView.html',
        '/assets/javascripts/admin/centres/directives/centreSummary/centreSummary.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html',
        '/assets/javascripts/common/directives/statusLine/statusLine.html');

      spyOn(modalService, 'modalOkCancel').and.returnValue(this.$q.when('ok'));
    }));

    it('initialization is valid', function() {
      this.createController();
      expect(this.scope.vm.centre).toBe(this.centre);
      expect(this.controller.descriptionToggleLength).toBeDefined();
      expect(this.eventRxFunc).toHaveBeenCalled();
    });

    describe('updates to name', function () {

      var context = {};

      beforeEach(inject(function () {
          var self = this,
              centre = new self.Centre(self.factory.centre());

        context.entity             = self.Centre;
        context.createController   = createController;
        context.updateFuncName     = 'updateName';
        context.controllerFuncName = 'editName';
        context.modalInputFuncName = 'text';

        function createController() {
          self.createController(centre);
        }
      }));

      entityUpdateSharedSpec(context);

    });

    describe('updates to description', function () {

      var context = {};

      beforeEach(inject(function () {
          var self = this,
              centre = new self.Centre(self.factory.centre());

        context.entity             = this.Centre;
        context.createController   = createController;
        context.updateFuncName     = 'updateDescription';
        context.controllerFuncName = 'editDescription';
        context.modalInputFuncName = 'textArea';

        function createController() {
          self.createController(centre);
        }
      }));

      entityUpdateSharedSpec(context);

    });

    describe('centre status ', function() {

      describe('enabling a centre', function() {
        var context = {};

        beforeEach(inject(function () {
          var self = this,
              centre = new self.Centre(self.factory.centre());

          context.createController = createController;
          context.centre           = centre;
          context.status           = 'enable';
          context.entity           = self.Centre;

          function createController() {
            self.createController(centre);
          }
        }));

        sharedCentreStatusBehaviour(context);
      });

      describe('disabling a centre', function() {
        var context = {};

        beforeEach(inject(function () {
          var self = this,
              centre = new self.Centre(self.factory.centre({ status: self.CentreStatus.ENABLED }));

          context.createController = createController;
          context.centre           = centre;
          context.status           = 'disable';
          context.entity           = this.Centre;

          function createController() {
            self.createController(centre);
          }
        }));

        sharedCentreStatusBehaviour(context);
      });

      it('changing status to an invalid value causes an exception', function() {
        var self = this,
            invalidStatus = self.factory.stringNext();
        self.createController();
        expect(function () { self.controller.changeStatus(invalidStatus); })
          .toThrowError(/invalid status/);
      });

    });

    function sharedCentreStatusBehaviour(context) {

      describe('(shared) study status', function () {

        it('change status', function () {
          spyOn(context.entity.prototype, context.status).and.returnValue(this.$q.when(context.centre));

          context.createController();
          this.controller.changeStatus(context.status);
          this.scope.$digest();
          expect(context.entity.prototype[context.status]).toHaveBeenCalled();
          expect(this.scope.vm.centre).toBe(context.centre);
        });

      });
    }

  });

});
