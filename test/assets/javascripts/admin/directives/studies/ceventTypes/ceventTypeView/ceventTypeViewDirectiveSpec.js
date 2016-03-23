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
      entityUpdateSharedSpec = require('../../entityUpdateSharedSpec');

  describe('ceventTypeViewDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, testUtils, directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q                   = self.$injector.get('$q');
      self.$state               = self.$injector.get('$state');
      self.CollectionEventType  = self.$injector.get('CollectionEventType');
      self.AnnotationType       = self.$injector.get('AnnotationType');
      self.notificationsService = self.$injector.get('notificationsService');
      self.jsonEntities         = self.$injector.get('jsonEntities');

      self.jsonStudy           = this.jsonEntities.study();
      self.jsonCet             = self.jsonEntities.collectionEventType(self.jsonStudy);
      self.collectionEventType = new self.CollectionEventType(self.jsonCet);

      spyOn(this.$state, 'go').and.returnValue(true);

      self.createController = createController;

      this.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/collection/ceventTypeView/ceventTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle.html',
        '/assets/javascripts/admin/directives/annotationTypeSummary/annotationTypeSummary.html',
        '/assets/javascripts/admin/directives/studies/collection/collectionSpecimenSpecSummary/collectionSpecimenSpecSummary.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html',
        '/assets/javascripts/admin/directives/statusLine/statusLine.html',
        '/assets/javascripts/common/modalInput/modalInput.html');

      function createController() {
        self.element = angular.element(
          '<cevent-type-view cevent-type="vm.ceventType"></cevent-type-view>');

        self.scope = $rootScope.$new();
        self.scope.vm = { ceventType: self.collectionEventType };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('ceventTypeView');
      }

    }));

    it('scope should be valid', function() {
      this.createController();
      expect(this.controller.ceventType).toBe(this.collectionEventType);
    });

    it('calling addAnnotationType should change to the correct state', function() {
      this.createController();
      this.controller.addAnnotationType();
      this.scope.$digest();
      expect(this.$state.go)
        .toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType.annotationTypeAdd');
    });

    it('calling addSpecimenSpec should change to the correct state', function() {
      this.createController();
      this.controller.addSpecimenSpec();
      this.scope.$digest();
      expect(this.$state.go)
        .toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType.specimenSpecAdd');
    });

    it('calling editAnnotationType should change to the correct state', function() {
      var annotType = new this.AnnotationType(this.jsonEntities.annotationType());

      this.createController();
      this.controller.editAnnotationType(annotType);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventType.annotationTypeView',
        { annotationTypeId: annotType.uniqueId });
    });

    describe('updates to name', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity             = this.CollectionEventType;
        context.updateFuncName     = 'updateName';
        context.controllerFuncName = 'editName';
        context.modalInputFuncName = 'text';
        context.newValue           = this.jsonEntities.stringNext();
      }));

      entityUpdateSharedSpec(context);

    });

    describe('updates to description', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity             = this.CollectionEventType;
        context.updateFuncName     = 'updateDescription';
        context.controllerFuncName = 'editDescription';
        context.modalInputFuncName = 'textArea';
        context.newValue           = this.jsonEntities.stringNext();
      }));

      entityUpdateSharedSpec(context);

    });

    describe('updates to recurring', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity               = this.CollectionEventType;
        context.updateFuncName       = 'updateRecurring';
        context.controllerFuncName   = 'editRecurring';
        context.modalInputFuncName = 'boolean';
        context.newValue             = false;
      }));

      entityUpdateSharedSpec(context);

    });

  });

});
