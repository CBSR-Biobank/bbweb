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
                              'CentreState',
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

    describe('centre state ', function() {

      describe('enabling a centre', function() {
        var context = {};

        beforeEach(inject(function () {
          var self = this,
              centre = new self.Centre(self.factory.centre());

          context.createController = createController;
          context.centre           = centre;
          context.state           = 'enable';
          context.entity           = self.Centre;

          function createController() {
            self.createController(centre);
          }
        }));

        sharedCentreStateBehaviour(context);
      });

      describe('disabling a centre', function() {
        var context = {};

        beforeEach(inject(function () {
          var self = this,
              centre = new self.Centre(self.factory.centre({ state: self.CentreState.ENABLED }));

          context.createController = createController;
          context.centre           = centre;
          context.state           = 'disable';
          context.entity           = this.Centre;

          function createController() {
            self.createController(centre);
          }
        }));

        sharedCentreStateBehaviour(context);
      });

      it('changing state to an invalid value causes an exception', function() {
        var self = this,
            invalidState = self.factory.stringNext();
        self.createController();
        expect(function () { self.controller.changeState(invalidState); })
          .toThrowError(/invalid state/);
      });

    });

    function sharedCentreStateBehaviour(context) {

      describe('(shared) study state', function () {

        it('change state', function () {
          spyOn(context.entity.prototype, context.state).and.returnValue(this.$q.when(context.centre));

          context.createController();
          this.controller.changeState(context.state);
          this.scope.$digest();
          expect(context.entity.prototype[context.state]).toHaveBeenCalled();
          expect(this.scope.vm.centre).toBe(context.centre);
        });

      });
    }

  });

});
