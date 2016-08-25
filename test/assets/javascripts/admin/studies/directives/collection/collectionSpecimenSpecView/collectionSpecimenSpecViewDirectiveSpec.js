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
      _                      = require('lodash'),
      faker                  = require('faker');

  describe('collectionSpecimenSpecViewDirective', function() {

    var createController = function () {
      this.element = angular.element([
        '<collection-specimen-spec-view',
        '  study="vm.study"',
        '  collection-event-type="vm.collectionEventType"',
        '  specimen-spec="vm.specimenSpec">',
        '</collection-specimen-spec-view>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        study:               this.study,
        collectionEventType: this.collectionEventType,
        specimenSpec:        this.specimenSpec
      };

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('collectionSpecimenSpecView');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Study',
                              'CollectionEventType',
                              'CollectionSpecimenSpec',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenSpecView/collectionSpecimenSpecView.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html');

      self.jsonSpecimenSpec    = self.factory.collectionSpecimenSpec();
      self.jsonCeventType      = self.factory.collectionEventType({
        specimenSpecs: [ self.jsonSpecimenSpec ]
      });
      self.jsonStudy           = self.factory.defaultStudy;

      self.study               = new self.Study(self.jsonStudy);
      self.collectionEventType = new self.CollectionEventType(self.jsonCeventType);
      self.specimenSpec        = new self.CollectionSpecimenSpec(self.jsonSpecimenSpec);

    }));

    it('should have valid scope', function() {
      createController.call(this);

      expect(this.controller.study).toBe(this.study);
      expect(this.controller.collectionEventType).toBe(this.collectionEventType);
      expect(this.controller.specimenSpec).toBe(this.specimenSpec);

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

      createController.call(this);
      this.controller.back();
      this.scope.$digest();

      expect($state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventType',
        { ceventTypeId: this.collectionEventType.id }
      );
    });

    describe('updates to name', function () {

      var context = {};

      beforeEach(inject(function () {
        context.createController         = createController;
        context.controllerUpdateFuncName = 'editName';
        context.modalInputFuncName       = 'text';
        context.ceventType               = this.collectionEventType;
        context.newValue                 = faker.lorem.word();
      }));

      sharedBehaviour(context);

    });

    describe('updates to description', function () {

      var context = {};

      beforeEach(inject(function () {
        context.createController         = createController;
        context.controllerUpdateFuncName = 'editDescription';
        context.modalInputFuncName       = 'textArea';
        context.ceventType               = this.collectionEventType;
        context.newValue                 = faker.lorem.sentences(4);
      }));

      sharedBehaviour(context);

    });

    describe('updates to anatimical source', function () {

      var context = {};

      beforeEach(inject(function () {
        this.injectDependencies('AnatomicalSourceType');

        context.createController         = createController;
        context.controllerUpdateFuncName = 'editAnatomicalSource';
        context.modalInputFuncName       = 'select';
        context.ceventType               = this.collectionEventType;
        context.newValue                 = this.AnatomicalSourceType.BLOOD;
      }));

      sharedBehaviour(context);

    });

    describe('updates to preservation type', function () {

      var context = {};

      beforeEach(inject(function () {
        this.injectDependencies('PreservationType');

        context.createController         = createController;
        context.controllerUpdateFuncName = 'editPreservationType';
        context.modalInputFuncName       = 'select';
        context.ceventType               = this.collectionEventType;
        context.newValue                 = this.PreservationType.FRESH_SPECIMEN;
      }));

      sharedBehaviour(context);

    });

    describe('updates to preservation temperature', function () {

      var context = {};

      beforeEach(inject(function () {
        this.injectDependencies('PreservationTemperatureType');

        context.createController         = createController;
        context.controllerUpdateFuncName = 'editPreservationTemperature';
        context.modalInputFuncName       = 'select';
        context.ceventType               = this.collectionEventType;
        context.newValue                 = this.PreservationTemperatureType.ROOM_TEMPERATURE;
      }));

      sharedBehaviour(context);

    });

    describe('updates to specimen type', function () {

      var context = {};

      beforeEach(inject(function () {
        this.injectDependencies('SpecimenType');

        context.createController         = createController;
        context.controllerUpdateFuncName = 'editSpecimenType';
        context.modalInputFuncName       = 'select';
        context.ceventType               = this.collectionEventType;
        context.newValue                 = this.SpecimenType.BUFFY_COAT;
      }));

      sharedBehaviour(context);

    });

    describe('updates to units', function () {

      var context = {};

      beforeEach(inject(function () {
        context.createController         = createController;
        context.controllerUpdateFuncName = 'editUnits';
        context.modalInputFuncName       = 'text';
        context.ceventType               = this.collectionEventType;
        context.newValue                 = 'ounces';
      }));

      sharedBehaviour(context);

    });

    describe('updates to amount', function () {

      var context = {};

      beforeEach(inject(function () {
        context.createController         = createController;
        context.controllerUpdateFuncName = 'editAmount';
        context.modalInputFuncName       = 'positiveFloat';
        context.ceventType               = this.collectionEventType;
        context.newValue                 = '0.2';
      }));

      sharedBehaviour(context);

    });

    describe('updates to count', function () {

      var context = {};

      beforeEach(inject(function () {
        context.createController         = createController;
        context.controllerUpdateFuncName = 'editMaxCount';
        context.modalInputFuncName       = 'naturalNumber';
        context.ceventType               = this.collectionEventType;
        context.newValue                 = '2';
      }));

      sharedBehaviour(context);

    });

  });

  /**
   *
   * @param {object} context.ceventType the collection event type that contains the specimen spec.
   *
   * @param {CollectionSpecimenSpec} context.specimenSpec the specimen spec to be viewed.
   *
   * @param {function} context.createController is a function that creates the controller and scope:
   * this.controller, and this.scope.
   */
  function sharedBehaviour(context) {

    describe('(shared) tests', function() {

      beforeEach(inject(function() {
        this.injectDependencies('modalInput', 'notificationsService');
      }));

      it('on update should invoke the update method on entity', function() {
        var deferred = this.$q.defer();

        deferred.resolve(context.newValue);

        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: deferred.promise});
        spyOn(this.CollectionEventType.prototype, 'updateSpecimenSpec')
          .and.returnValue(this.$q.when(context.ceventType));
        spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

        context.createController.call(this);
        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();

        expect(this.CollectionEventType.prototype.updateSpecimenSpec).toHaveBeenCalled();
        expect(this.notificationsService.success).toHaveBeenCalled();
      });

      it('error message should be displayed when update fails', function() {
        var deferred = this.$q.defer(),
            updateDeferred = this.$q.defer();

        deferred.resolve(context.newValue);
        updateDeferred.reject('simulated error');

        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: deferred.promise});
        spyOn(this.CollectionEventType.prototype, 'updateSpecimenSpec')
          .and.returnValue(updateDeferred.promise);
        spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));

        context.createController.call(this);
        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();
        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });
  }

});
