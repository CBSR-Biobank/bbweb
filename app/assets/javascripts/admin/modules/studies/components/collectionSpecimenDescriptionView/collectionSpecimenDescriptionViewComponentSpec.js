/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import faker from 'faker';
import ngModule from '../../index'

describe('Component: collectionSpecimenDescriptionView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Study',
                              'CollectionEventType',
                              'CollectionSpecimenDescription',
                              'Factory');

      this.jsonSpecimenDescription = this.Factory.collectionSpecimenDescription();
      this.jsonCeventType          = this.Factory.collectionEventType({
        specimenDescriptions: [ this.jsonSpecimenDescription ]
      });
      this.jsonStudy           = this.Factory.defaultStudy;

      this.study               = new this.Study(this.jsonStudy);
      this.collectionEventType = new this.CollectionEventType(this.jsonCeventType);
      this.specimenDescription = new this.CollectionSpecimenDescription(this.jsonSpecimenDescription);
      this.createController = () => {
        this.CollectionEventType.get = jasmine.createSpy()
          .and.returnValue(this.$q.when(this.collectionEventType));

        ComponentTestSuiteMixin.createController.call(
          this,
          [
            '<collection-specimen-description-view',
            '  study="vm.study"',
            '  collection-event-type="vm.collectionEventType"',
            '  specimen-description="vm.specimenDescription">',
            '</collection-specimen-description-view>'
          ].join(''),
          {
            study:               this.study,
            collectionEventType: this.collectionEventType,
            specimenDescription: this.specimenDescription
          },
          'collectionSpecimenDescriptionView');
      };
    });
  });

  it('should have valid scope', function() {
    this.createController();

    expect(this.controller.study).toBe(this.study);
    expect(this.controller.collectionEventType).toBe(this.collectionEventType);
    expect(this.controller.specimenDescription).toBe(this.specimenDescription);

    expect(this.controller.editName).toBeFunction();
    expect(this.controller.editDescription).toBeFunction();
    expect(this.controller.editAnatomicalSource).toBeFunction();
    expect(this.controller.editPreservationType).toBeFunction();
    expect(this.controller.editPreservationTemperature).toBeFunction();
    expect(this.controller.editSpecimenType).toBeFunction();
    expect(this.controller.editUnits).toBeFunction();
    expect(this.controller.editAmount).toBeFunction();
    expect(this.controller.editMaxCount).toBeFunction();
    expect(this.controller.back).toBeFunction();
  });

  it('calling back returns to the proper state', function() {
    var $state = this.$injector.get('$state');

    spyOn($state, 'go').and.returnValue('ok');

    this.createController();
    this.controller.back();
    this.scope.$digest();

    expect($state.go).toHaveBeenCalledWith(
      'home.admin.studies.study.collection.ceventType',
      { ceventTypeId: this.collectionEventType.id }
    );
  });

  describe('updates to name', function () {

    var context = {};

    beforeEach(function () {
      context.createController         = this.createController.bind(this);
      context.controllerUpdateFuncName = 'editName';
      context.modalInputFuncName       = 'text';
      context.ceventType               = this.collectionEventType;
      context.newValue                 = faker.lorem.word();
    });

    sharedBehaviour(context);

  });

  describe('updates to description', function () {

    var context = {};

    beforeEach(function () {
      context.createController         = this.createController.bind(this);
      context.controllerUpdateFuncName = 'editDescription';
      context.modalInputFuncName       = 'textArea';
      context.ceventType               = this.collectionEventType;
      context.newValue                 = faker.lorem.sentences(4);
    });

    sharedBehaviour(context);

  });

  describe('updates to anatimical source', function () {

    var context = {};

    beforeEach(function () {
      this.injectDependencies('AnatomicalSourceType');

      context.createController         = this.createController.bind(this);
      context.controllerUpdateFuncName = 'editAnatomicalSource';
      context.modalInputFuncName       = 'select';
      context.ceventType               = this.collectionEventType;
      context.newValue                 = this.AnatomicalSourceType.BLOOD;
    });

    sharedBehaviour(context);

  });

  describe('updates to preservation type', function () {

    var context = {};

    beforeEach(function () {
      this.injectDependencies('PreservationType');

      context.createController         = this.createController.bind(this);
      context.controllerUpdateFuncName = 'editPreservationType';
      context.modalInputFuncName       = 'select';
      context.ceventType               = this.collectionEventType;
      context.newValue                 = this.PreservationType.FRESH_SPECIMEN;
    });

    sharedBehaviour(context);

  });

  describe('updates to preservation temperature', function () {

    var context = {};

    beforeEach(function () {
      this.injectDependencies('PreservationTemperature');

      context.createController         = this.createController.bind(this);
      context.controllerUpdateFuncName = 'editPreservationTemperature';
      context.modalInputFuncName       = 'select';
      context.ceventType               = this.collectionEventType;
      context.newValue                 = this.PreservationTemperature.ROOM_TEMPERATURE;
    });

    sharedBehaviour(context);

  });

  describe('updates to specimen type', function () {

    var context = {};

    beforeEach(function () {
      this.injectDependencies('SpecimenType');

      context.createController         = this.createController.bind(this);
      context.controllerUpdateFuncName = 'editSpecimenType';
      context.modalInputFuncName       = 'select';
      context.ceventType               = this.collectionEventType;
      context.newValue                 = this.SpecimenType.BUFFY_COAT;
    });

    sharedBehaviour(context);

  });

  describe('updates to units', function () {

    var context = {};

    beforeEach(function () {
      context.createController         = this.createController.bind(this);
      context.controllerUpdateFuncName = 'editUnits';
      context.modalInputFuncName       = 'text';
      context.ceventType               = this.collectionEventType;
      context.newValue                 = 'ounces';
    });

    sharedBehaviour(context);

  });

  describe('updates to amount', function () {

    var context = {};

    beforeEach(function () {
      context.createController         = this.createController.bind(this);
      context.controllerUpdateFuncName = 'editAmount';
      context.modalInputFuncName       = 'positiveFloat';
      context.ceventType               = this.collectionEventType;
      context.newValue                 = '0.2';
    });

    sharedBehaviour(context);

  });

  describe('updates to count', function () {

    var context = {};

    beforeEach(function () {
      context.createController         = this.createController.bind(this);
      context.controllerUpdateFuncName = 'editMaxCount';
      context.modalInputFuncName       = 'naturalNumber';
      context.ceventType               = this.collectionEventType;
      context.newValue                 = '2';
    });

    sharedBehaviour(context);

  });

  /**
   *
   * @param {object} context.ceventType the collection event type that contains the specimen spec.
   *
   * @param {CollectionSpecimenDescription} context.specimenDescription the specimen spec to be viewed.
   *
   * @param {function} context.createController is a function that creates the controller and scope:
   * this.controller, and this.scope.
   */
  function sharedBehaviour(context) {

    describe('(shared) tests', function() {

      beforeEach(function() {
        this.injectDependencies('modalInput', 'notificationsService');
      });

      it('on update should invoke the update method on entity', function() {
        var deferred = this.$q.defer();

        deferred.resolve(context.newValue);

        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: deferred.promise});
        spyOn(this.CollectionEventType.prototype, 'updateSpecimenDescription')
          .and.returnValue(this.$q.when(context.ceventType));
        spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

        context.createController.call(this);
        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();

        expect(this.CollectionEventType.prototype.updateSpecimenDescription).toHaveBeenCalled();
        expect(this.notificationsService.success).toHaveBeenCalled();
      });

      it('error message should be displayed when update fails', function() {
        context.createController.call(this);

        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: this.$q.when(context.newValue) });
        spyOn(this.CollectionEventType.prototype, 'updateSpecimenDescription')
          .and.returnValue(this.$q.reject('simulated error'));
        spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));

        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();
        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });
  }

});
