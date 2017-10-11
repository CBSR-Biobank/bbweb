/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import entityUpdateSharedSpec from '../../../../test/entityUpdateSharedSpec';

describe('Component: centreSummary', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Centre',
                              'CentreState',
                              'notificationsService',
                              'modalService',
                              'factory');
      this.centre = new this.Centre(this.factory.centre());
      this.createScope = () => {
        var scope = ComponentTestSuiteMixin.createScope.call(this, { centre: this.centre });
        this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
        scope.$on('tabbed-page-update', this.eventRxFunc);
        return scope;
      };

      this.createController = (centre) =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<centre-summary centre="vm.centre"></centre-summary>',
          { centre: centre },
          'centreSummary');

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('ok'));
    });
  });

  it('initialization is valid', function() {
    this.createController();
    expect(this.scope.vm.centre).toBe(this.centre);
    expect(this.controller.descriptionToggleLength).toBeDefined();
    expect(this.eventRxFunc).toHaveBeenCalled();
  });

  describe('updates to name', function () {

    var context = {};

    beforeEach(function () {
      var self = this,
          centre = new this.Centre(this.factory.centre());

      context.entity             = self.Centre;
      context.createController   = this.createController.bind(this, centre);
      context.updateFuncName     = 'updateName';
      context.controllerFuncName = 'editName';
      context.modalInputFuncName = 'text';
    });

    entityUpdateSharedSpec(context);

  });

  describe('updates to description', function () {

    var context = {};

    beforeEach(function () {
      var self = this,
          centre = new self.Centre(self.factory.centre());

      context.entity             = this.Centre;
      context.createController   = this.createController.bind(this, centre);
      context.updateFuncName     = 'updateDescription';
      context.controllerFuncName = 'editDescription';
      context.modalInputFuncName = 'textArea';
    });

    entityUpdateSharedSpec(context);

  });

  describe('centre state ', function() {

    describe('enabling a centre', function() {
      var context = {};

      beforeEach(function () {
        var self = this,
            centre = new self.Centre(self.factory.centre());

        context.createController = this.createController.bind(this, centre);
        context.centre           = centre;
        context.state            = 'enable';
        context.entity           = self.Centre;
      });

      sharedCentreStateBehaviour(context);
    });

    describe('disabling a centre', function() {
      var context = {};

      beforeEach(function () {
        var self = this,
            centre = new self.Centre(self.factory.centre({ state: self.CentreState.ENABLED }));

        context.createController = this.createController.bind(this, centre);
        context.centre           = centre;
        context.state            = 'disable';
        context.entity           = this.Centre;
      });

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
        expect(this.controller.centre).toBe(context.centre);
      });

    });
  }

});
