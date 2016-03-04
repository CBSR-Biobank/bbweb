/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('annotationTypeViewDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, testUtils) {
      var self = this;

      self.$q             = self.$injector.get('$q');
      self.Study          = self.$injector.get('Study');
      self.AnnotationType = self.$injector.get('AnnotationType');
      self.jsonEntities   = self.$injector.get('jsonEntities');

      testUtils.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/annotationTypes/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle.html',
        '/assets/javascripts/common/services/modalStringInput.html');

      self.createController = setupController();
      self.returnState      = 'my-return-state';
      self.study            = new self.Study(self.jsonEntities.study());
      self.annotationType   = new self.AnnotationType(self.jsonEntities.annotationType());
      self.onUpdate         = jasmine.createSpy('onUpdate').and.returnValue(self.$q.when(self.study));

      function setupController() {
        return create;

        function create() {
          self.element = angular.element([
            '<annotation-type-view ',
            '  study="vm.study"',
            '  annotation-type="vm.annotationType"',
            '  return-state="' + self.returnState  + '"',
            '  on-update="vm.onUpdate"',
            '</annotation-type-view>',
          ].join(''));

          self.scope = $rootScope.$new();
          self.scope.vm = {
            study:          self.study,
            annotationType: self.annotationType,
            returnState:    self.returnState,
            onUpdate:       self.onUpdate
          };

          $compile(self.element)(self.scope);
          self.scope.$digest();
          self.controller = self.element.controller('annotationTypeView');
        }
      }
    }));

    it('should have valid scope', function() {
      this.createController(this.study, this.annotationType);
      expect(this.controller.annotationType).toBe(this.annotationType);
      expect(this.controller.returnState).toBe(this.returnState);
      expect(this.controller.onUpdate).toBeFunction();
    });

    it('call to back function returns to valid state', function() {
      var $state = this.$injector.get('$state');

      spyOn($state, 'go').and.returnValue(0);

      this.createController(this.study, this.annotationType);
      this.controller.back();
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState, {}, { reload: true });
    });


    describe('updates to name', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'editName';
        context.modalServiceFuncName = 'modalTextInput';
      }));

      sharedUpdateBehaviour(context);

    });


    describe('updates to required', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'editRequired';
        context.modalServiceFuncName = 'modalBooleanInput';
      }));

      sharedUpdateBehaviour(context);

    });

    describe('updates to description', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'editDescription';
        context.modalServiceFuncName = 'modalTextAreaInput';
      }));

      sharedUpdateBehaviour(context);

    });

    describe('add selections', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'addSelectionOptions';
        context.modalServiceFuncName = 'modalCommaDelimitedInput';
      }));

      sharedUpdateBehaviour(context);

    });

    function sharedUpdateBehaviour(context) {

      beforeEach(inject(function () {
        var self = this;

        self.modalService         = self.$injector.get('modalService');
        self.notificationsService = self.$injector.get('notificationsService');
      }));

      describe('(shared) update functions', function () {

        it('should update a field on a study', function() {
          var newValue = this.jsonEntities.stringNext();

          spyOn(this.modalService, context.modalServiceFuncName).and.returnValue(this.$q.when(newValue));

          this.createController();
          expect(this.controller[context.controllerFuncName]).toBeFunction();
          this.controller[context.controllerFuncName]();
          this.scope.$digest();
          expect(this.onUpdate).toHaveBeenCalled();
        });

      });
    }
  });

});
