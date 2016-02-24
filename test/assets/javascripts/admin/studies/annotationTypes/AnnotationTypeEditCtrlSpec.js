/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: AnnotationTypeEditCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    describe('for collection event annotation types', function() {
      var context = {};

      beforeEach(inject(function (CollectionEventAnnotationType, jsonEntities) {
        var baseAnnotationType = jsonEntities.annotationType();

        context.state                = { current: { name: 'home.admin.studies.study.collection.view' } };
        context.returnState          = 'home.admin.studies.study.collection.view';
        context.annotationTypeNew    = new CollectionEventAnnotationType(_.omit(baseAnnotationType, 'id'));
        context.annotationTypeWithId = new CollectionEventAnnotationType(baseAnnotationType);
      }));

      sharedBehaviour(context);
    });

    describe('for specimen link annotation types', function() {
      var context = {};

      beforeEach(inject(function (SpecimenLinkAnnotationType, jsonEntities) {
        var baseAnnotationType = jsonEntities.annotationType({ required: true }),
            stateName = 'home.admin.studies.study.processing';

        context.state           = { current: { name: stateName } };
        context.returnState     = stateName;
        context.annotationTypeNew    = new SpecimenLinkAnnotationType(_.omit(baseAnnotationType, 'id'));
        context.annotationTypeWithId = new SpecimenLinkAnnotationType(baseAnnotationType);
      }));

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      describe('(shared)', function() {

        beforeEach(inject(function ($state, Study, jsonEntities) {
          this.$q                        = this.$injector.get('$q');
          this.$state                    = this.$injector.get('$state');
          this.$rootScope                = this.$injector.get('$rootScope');
          this.$controller               = this.$injector.get('$controller');
          this.domainEntityService       = this.$injector.get('domainEntityService');
          this.AnnotationValueType       = this.$injector.get('AnnotationValueType');
          this.context                   = context;

          spyOn($state, 'go').and.callFake(function () {} );

          this.study = new Study(jsonEntities.study());
        }));

        it('scope should be valid when adding', function() {
          this.annotationType = this.context.annotationTypeNew;

          createController(this);

          expect(this.scope.vm.study).toEqual(this.study);
          expect(this.scope.vm.annotationType).toEqual(this.annotationType);
          expect(this.scope.vm.title).toBe('Add Annotation Type');
          expect(this.scope.vm.hasRequiredField).toEqual(true);
          expect(this.scope.vm.valueTypes).toEqual(this.AnnotationValueType.values());
        });

        it('scope should be valid when updating', function() {
          this.annotationType = this.context.annotationTypeWithId;

          createController(this);

          expect(this.scope.vm.study).toEqual(this.study);
          expect(this.scope.vm.annotationType).toEqual(this.annotationType);
          expect(this.scope.vm.title).toBe('Update Annotation Type');
          expect(this.scope.vm.hasRequiredField)
            .toEqual(this.annotationType instanceof this.ParticipantAnnotationType);
          expect(this.scope.vm.valueTypes).toEqual(this.AnnotationValueType.values());
        });

        it('throws an exception if the current state is invalid', function() {
          var self = this,
              invalidStateName = 'xyz';

          this.context.state = { current: { name: invalidStateName } };
          this.annotationType = this.context.annotationTypeWithId;
          expect(function () {
            createController(self);
          }).toThrow(new Error('invalid current state name: ' + invalidStateName));
        });

        it('maxValueCountRequired is valid', function() {
          this.annotationType = this.context.annotationTypeWithId;

          this.annotationType.valueType = this.AnnotationValueType.SELECT();
          this.annotationType.maxValueCount = 0;
          createController(this);

          this.annotationType.maxValueCount = 0;
          expect(this.scope.vm.maxValueCountRequired()).toBe(true);

          this.annotationType.maxValueCount = 3;
          expect(this.scope.vm.maxValueCountRequired()).toBe(true);

          this.annotationType.maxValueCount = 1;
          expect(this.scope.vm.maxValueCountRequired()).toBe(false);

          this.annotationType.maxValueCount = 2;
          expect(this.scope.vm.maxValueCountRequired()).toBe(false);
        });

        it('calling valueTypeChange clears the options array', function() {
          this.annotationType = this.context.annotationTypeWithId;

          this.annotationType.valueType = 'Select';
          this.annotationType.maxValueCount = 1;
          createController(this);

          this.scope.vm.valueTypeChange();
          expect(this.annotationType.options).toBeArray();
          expect(this.annotationType.options).toBeEmptyArray();
        });

        it('calling optionAdd creates the options array', function() {
          this.annotationType = this.context.annotationTypeWithId;

          createController(this);
          this.annotationType.valueType = 'Select';
          this.annotationType.maxValueCount = 1;
          this.annotationType.valueTypeChanged();

          this.scope.vm.optionAdd();
          expect(this.annotationType.options).toBeArrayOfSize(1);
          expect(this.annotationType.options).toBeArrayOfStrings();
        });

        it('calling optionAdd appends to the options array', function() {
          this.annotationType = this.context.annotationTypeWithId;

          this.annotationType.valueType = 'Select';
          this.annotationType.maxValueCount = 1;
          this.annotationType.valueTypeChanged();
          createController(this);

          this.scope.vm.optionAdd();
          expect(this.annotationType.options).toBeArrayOfSize(1);
          expect(this.annotationType.options).toBeArrayOfStrings();
        });

        it('calling optionRemove throws an error on empty array', function() {
          this.annotationType = this.context.annotationTypeWithId;

          this.annotationType.valueType = 'Select';
          this.annotationType.maxValueCount = 1;
          this.annotationType.valueTypeChanged();
          createController(this);
          expect(function () { this.scope.vm.optionRemove('abc'); }).toThrow();
        });

        it('calling optionRemove throws an error if removal results in empty array', function() {
          this.annotationType = this.context.annotationTypeWithId;

          this.annotationType.valueType = 'Select';
          this.annotationType.maxValueCount = 1;
          this.annotationType.valueTypeChanged();
          this.annotationType.options = ['abc'];
          createController(this);
          expect(function () { this.scope.vm.optionRemove('abc'); }).toThrow();
        });

        it('calling optionRemove removes an option', function() {
          this.annotationType = this.context.annotationTypeWithId;

          // note: more than two strings in options array
          var options = ['abc', 'def'];
          this.annotationType.valueType = 'Select';
          this.annotationType.maxValueCount = 1;
          this.annotationType.valueTypeChanged();
          this.annotationType.options = options.slice(0);
          createController(this);
          this.scope.vm.optionRemove('abc');
          expect(this.annotationType.options).toBeArrayOfSize(options.length - 1);
        });

        it('calling removeButtonDisabled returns valid results', function() {
          this.annotationType = this.context.annotationTypeWithId;

          // note: more than two strings in options array
          var options = ['abc', 'def'];
          this.annotationType.valueType = 'Select';
          this.annotationType.maxValueCount = 1;
          this.annotationType.valueTypeChanged();
          this.annotationType.options = options.slice(0);
          createController(this);

          expect(this.scope.vm.removeButtonDisabled()).toEqual(false);

          this.annotationType.options = options.slice(1);
          expect(this.scope.vm.removeButtonDisabled()).toEqual(true);
        });

        it('when adding should return to the valid state on submit', function() {
          this.annotationType = this.context.annotationTypeNew;
          onSubmit(this);
        });

        it('when adding should return to the valid state on cancel', function() {
          this.annotationType = this.context.annotationTypeNew;
          onCancel(this);
        });

        it('when submitting and server responds with an error, the error message is displayed', function() {
          this.annotationType = this.context.annotationTypeNew;

          spyOn(this.domainEntityService, 'updateErrorModal')
            .and.callFake(function () {});

          spyOnAnnotationTypeAddOrUpdateAndReject(this);

          createController(this);
          this.scope.vm.submit(this.context.annotationTypeNew);
          this.scope.$digest();

          expect(this.domainEntityService.updateErrorModal).toHaveBeenCalled();
        });

        it('when updating should return to the valid state on submit', function() {
          this.annotationType = this.context.annotationTypeWithId;
          onSubmit(this);
        });

        it('when updating should return to the valid state on cancel', function() {
          this.annotationType = this.context.annotationTypeWithId;
          onCancel(this);
        });

        function createController(userContext) {
          var notificationsService = userContext.$injector.get('notificationsService');

          userContext.scope = userContext.$rootScope.$new();
          userContext.$state.current.name = userContext.context.state.current.name;

          userContext.$controller('AnnotationTypeEditCtrl as vm', {
            $scope:                    userContext.scope,
            $state:                    userContext.$state,
            notificationsService:      notificationsService,
            domainEntityService:       userContext.domainEntityService,
            ParticipantAnnotationType: userContext.ParticipantAnnotationType,
            AnnotationValueType:       userContext.AnnotationValueType,
            study:                     userContext.study,
            annotationType:            userContext.annotationType
          });
          userContext.scope.$digest();
        }

        function spyOnAnnotationTypeAddOrUpdateAndResolve(userContext) {
          spyOn(userContext.annotationType, 'addOrUpdate').and.callFake(function () {
            return userContext.$q.when('');
          });
        }

        function spyOnAnnotationTypeAddOrUpdateAndReject(userContext) {
          spyOn(userContext.annotationType, 'addOrUpdate').and.callFake(function () {
            var deferred = userContext.$q.defer();
            deferred.reject({ data: { message: 'error'} });
            return deferred.promise;
          });
        }

        function onSubmit(userContext) {
          spyOnAnnotationTypeAddOrUpdateAndResolve(userContext);

          createController(userContext);
          userContext.scope.vm.submit(userContext.annotationType);
          userContext.scope.$digest();
          expect(userContext.$state.go).toHaveBeenCalledWith(
            userContext.context.returnState,
            {studyId: userContext.study.id}, {reload: true});
        }

        function onCancel(userContext) {
          spyOnAnnotationTypeAddOrUpdateAndResolve(userContext);

          createController(userContext);
          userContext.scope.vm.cancel();
          userContext.scope.$digest();
          expect(userContext.$state.go).toHaveBeenCalledWith(
            userContext.context.returnState,
            {studyId: userContext.study.id}, {reload: true});
        }

      });
    }

  });

});
