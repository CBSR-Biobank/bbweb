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

  fdescribe('Controller: AnnotationTypeEditCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    fdescribe('for participant annotation types', function() {
      var context = {};

      beforeEach(inject(function (Study) {
        var stateName = 'home.admin.studies.study.participants';

        context.state       = { current: { name: stateName } };
        context.returnState = stateName;
        context.annotationTypeAdd = annotationTypeAdd;

        function annotationTypeAdd(annotationType) {
          var study = new Study();
          return study.addAnnotationType(annotationType);
        }
      }));

      sharedBehaviour(context);
    });

    describe('for collection event annotation types', function() {
      var context = {};

      beforeEach(function () {
        context.state       = { current: { name: 'home.admin.studies.study.collection.view' } };
        context.returnState = 'home.admin.studies.study.collection.view';
      });

      sharedBehaviour(context);
    });

    describe('for specimen link annotation types', function() {
      var context = {};

      beforeEach(function () {
        var stateName = 'home.admin.studies.study.processing';

        context.state       = { current: { name: stateName } };
        context.returnState = stateName;
      });

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      describe('(shared)', function() {

        beforeEach(inject(function ($state, Study, jsonEntities) {
          this.$q                  = this.$injector.get('$q');
          this.$state              = this.$injector.get('$state');
          this.$rootScope          = this.$injector.get('$rootScope');
          this.$controller         = this.$injector.get('$controller');
          this.domainEntityService = this.$injector.get('domainEntityService');
          this.AnnotationType      = this.$injector.get('AnnotationType');
          this.AnnotationValueType = this.$injector.get('AnnotationValueType');
          this.jsonEntities        = this.$injector.get('jsonEntities');
          this.context             = context;

          spyOn($state, 'go').and.callFake(function () {} );

          this.study = new Study(jsonEntities.study());
        }));

        it('scope should be valid when adding', function() {
          createController(this);
          expect(this.scope.vm.study).toEqual(this.study);
          expect(this.scope.vm.annotationType).toEqual(jasmine.any(this.AnnotationType));
          expect(this.scope.vm.title).toBe('Add Annotation Type');
          expect(this.scope.vm.valueTypes).toEqual(this.AnnotationValueType.values());
        });

        it('throws an exception if the current state is invalid', function() {
          var self = this,
              invalidStateName = 'xyz';

          this.context.state = { current: { name: invalidStateName } };
          expect(function () {
            createController(self);
          }).toThrow(new Error('invalid current state name: ' + invalidStateName));
        });

        it('maxValueCountRequired is valid', function() {
          createController(this);

          this.scope.vm.annotationType.valueType = this.AnnotationValueType.SELECT();

          this.scope.vm.annotationType.maxValueCount = 0;
          expect(this.scope.vm.maxValueCountRequired()).toBe(true);

          this.scope.vm.annotationType.maxValueCount = 3;
          expect(this.scope.vm.maxValueCountRequired()).toBe(true);

          this.scope.vm.annotationType.maxValueCount = 1;
          expect(this.scope.vm.maxValueCountRequired()).toBe(false);

          this.scope.vm.annotationType.maxValueCount = 2;
          expect(this.scope.vm.maxValueCountRequired()).toBe(false);
        });

        it('calling valueTypeChange clears the options array', function() {
          this.annotationType = this.context.annotationTypeWithId;

          createController(this);

          this.scope.vm.annotationType.valueType = 'Select';
          this.scope.vm.annotationType.maxValueCount = 1;

          this.scope.vm.valueTypeChange();
          expect(this.scope.vm.annotationType.options).toBeArray();
          expect(this.scope.vm.annotationType.options).toBeEmptyArray();
        });

        it('calling optionAdd appends to the options array', function() {
          createController(this);

          this.scope.vm.annotationType.valueType = 'Select';
          this.scope.vm.annotationType.maxValueCount = 1;
          this.scope.vm.annotationType.valueTypeChanged();

          this.scope.vm.optionAdd();
          expect(this.scope.vm.annotationType.options).toBeArrayOfSize(1);
          expect(this.scope.vm.annotationType.options).toBeArrayOfStrings();
        });

        it('calling optionRemove throws an error on empty array', function() {
          createController(this);

          this.scope.vm.annotationType.valueType = 'Select';
          this.scope.vm.annotationType.maxValueCount = 1;
          this.scope.vm.annotationType.valueTypeChanged();
          expect(function () { this.scope.vm.optionRemove('abc'); }).toThrow();
        });

        it('calling optionRemove throws an error if removal results in empty array', function() {
          createController(this);

          this.scope.vm.annotationType.valueType = 'Select';
          this.scope.vm.annotationType.maxValueCount = 1;
          this.scope.vm.annotationType.valueTypeChanged();
          this.scope.vm.annotationType.options = ['abc'];
          expect(function () { this.scope.vm.optionRemove('abc'); }).toThrow();
        });

        it('calling optionRemove removes an option', function() {
          createController(this);

          // note: more than two strings in options array
          var options = ['abc', 'def'];
          this.scope.vm.annotationType.valueType = 'Select';
          this.scope.vm.annotationType.maxValueCount = 1;
          this.scope.vm.annotationType.valueTypeChanged();
          this.scope.vm.annotationType.options = options.slice(0);
          this.scope.vm.optionRemove('abc');
          expect(this.scope.vm.annotationType.options).toBeArrayOfSize(options.length - 1);
        });

        it('calling removeButtonDisabled returns valid results', function() {
          createController(this);

          // note: more than two strings in options array
          var options = ['abc', 'def'];
          this.scope.vm.annotationType.valueType = 'Select';
          this.scope.vm.annotationType.maxValueCount = 1;
          this.scope.vm.annotationType.valueTypeChanged();
          this.scope.vm.annotationType.options = options.slice(0);

          expect(this.scope.vm.removeButtonDisabled()).toEqual(false);

          this.scope.vm.annotationType.options = options.slice(1);
          expect(this.scope.vm.removeButtonDisabled()).toEqual(true);
        });

        fit('when adding should return to the valid state on submit', function() {
          onSubmit(this);
        });

        it('when adding should return to the valid state on cancel', function() {
          onCancel(this);
        });

        it('when submitting and server responds with an error, the error message is displayed', function() {
          this.scope.vm.annotationType = this.context.annotationTypeNew;

          spyOn(this.domainEntityService, 'updateErrorModal')
            .and.callFake(function () {});

          spyOnAnnotationTypeAddOrUpdateAndReject(this);

          createController(this);
          this.scope.vm.submit(this.context.annotationTypeNew);
          this.scope.$digest();

          expect(this.domainEntityService.updateErrorModal).toHaveBeenCalled();
        });

        it('when updating should return to the valid state on submit', function() {
          this.scope.vm.annotationType = this.context.annotationTypeWithId;
          onSubmit(this);
        });

        it('when updating should return to the valid state on cancel', function() {
          this.scope.vm.annotationType = this.context.annotationTypeWithId;
          onCancel(this);
        });

        function createController(userContext) {
          var notificationsService = userContext.$injector.get('notificationsService');

          userContext.scope = userContext.$rootScope.$new();
          userContext.$state.current.name = userContext.context.state.current.name;

          userContext.$controller('AnnotationTypeEditCtrl as vm', {
            $scope:                    userContext.scope,
            domainEntityService:       userContext.domainEntityService,
            AnnotationType:            userContext.AnnotationType,
            AnnotationValueType:       userContext.AnnotationValueType,
            study:                     userContext.study
          });
          userContext.scope.$digest();
        }

        function spyOnAnnotationTypeAddOrUpdateAndResolve(userContext) {
          spyOn(userContext.annotationType, 'addOrUpdate').and.callFake(function () {
            return userContext.$q.when('');
          });
        }

        function spyOnAnnotationTypeAddAndReject(userContext) {
          spyOn(userContext, 'addAnnotationType').and.callFake(function () {
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
