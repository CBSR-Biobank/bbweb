/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import entityUpdateSharedSpec from '../../../../test/entityUpdateSharedSpec';

describe('Component: studySummary', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      var specimenDescription, ceventType;

      _.extend(this, ComponentTestSuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Study',
                              'CollectionEventType',
                              'CollectionSpecimenDescription',
                              'modalService',
                              'notificationsService',
                              'factory');

      specimenDescription = this.factory.collectionSpecimenDescription();
      this.study = this.Study.create(this.factory.study());
      ceventType = this.CollectionEventType.create(
        this.factory.collectionEventType({ specimenDescriptions: [ specimenDescription ]}));

      spyOn(this.CollectionEventType, 'list').and.returnValue(this.$q.when([ ceventType ]));
      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when(true));

      this.study = new this.Study(this.factory.study());

      this.createScope = () => {
        var scope = ComponentTestSuiteMixin.prototype.createScope.call(this, { study: this.study });
        this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
        scope.$on('tabbed-page-update', this.eventRxFunc);
        return scope;
      };

      this.createController = (enableAllowed) => {
        if (_.isUndefined(enableAllowed)) {
          enableAllowed = true;
        }
        this.Study.prototype.isEnableAllowed =
          jasmine.createSpy().and.returnValue(this.$q.when(enableAllowed));

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<study-summary study="vm.study"></study-summary>',
          { study: this.study },
          'studySummary');
      };
    });
  });

  it('initialization is valid', function() {
    this.createController();
    expect(this.controller.study).toBe(this.study);
    expect(this.controller.descriptionToggleLength).toBeDefined();
    expect(this.controller.isEnableAllowed).toBeTrue();
    expect(this.eventRxFunc).toHaveBeenCalled();
  });

  it('should have valid settings when study has no collection event types', function() {
    this.createController(false);
    expect(this.controller.isEnableAllowed).toBeFalse();
  });

  describe('updates to name', function () {

    var context = {};

    beforeEach(function () {
      context.entity             = this.Study;
      context.createController   = this.createController.bind(this);
      context.updateFuncName     = 'updateName';
      context.controllerFuncName = 'editName';
      context.modalInputFuncName = 'text';
    });

    entityUpdateSharedSpec(context);

  });

  describe('updates to description', function () {
    var context = {};

    beforeEach(function () {
      context.entity             = this.Study;
      context.createController   = this.createController.bind(this);
      context.updateFuncName     = 'updateDescription';
      context.controllerFuncName = 'editDescription';
      context.modalInputFuncName = 'textArea';
    });

    entityUpdateSharedSpec(context);

  });

  describe('enabling a study', function() {
    var context = {};

    beforeEach(function () {
      context.state = 'enable';
    });

    sharedStudyStateBehaviour(context);
  });

  describe('disabling a study', function() {
    var context = {};

    beforeEach(function () {
      context.state = 'disable';
    });

    sharedStudyStateBehaviour(context);
  });

  describe('retiring a study', function() {
    var context = {};

    beforeEach(function () {
      context.state = 'retire';
    });

    sharedStudyStateBehaviour(context);
  });

  describe('unretiring a study', function() {
    var context = {};

    beforeEach(function () {
      context.state = 'unretire';
    });

    sharedStudyStateBehaviour(context);
  });


  function sharedStudyStateBehaviour(context) {

    describe('(shared) study state', function () {

      it('change state', function () {
        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('ok'));
        spyOn(this.Study, 'get').and.returnValue(this.$q.when(this.study));
        spyOn(this.Study.prototype, context.state).and.returnValue(this.$q.when(this.study));

        this.createController();
        this.controller.changeState(context.state);
        this.scope.$digest();
        expect(this.Study.prototype[context.state]).toHaveBeenCalled();
      });

    });
  }

  it('should throw error for when trying to change to an invalid state', function () {
    var self = this,
        badState = 'xxx';

    this.createController();
    expect(function () {
      self.controller.changeState(badState);
    }).toThrow(new Error('invalid state: ' + badState));
  });

});
