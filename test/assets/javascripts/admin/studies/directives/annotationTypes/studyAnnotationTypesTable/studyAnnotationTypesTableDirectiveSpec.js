/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: studyAnnotationTypesTableDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function () {
      var self = this;

      self.$q                   = self.$injector.get('$q');
      self.modalService         = self.$injector.get('modalService');
      self.Study                = self.$injector.get('Study');
      self.AnnotationType       = self.$injector.get('AnnotationType');
      self.AnnotationValueType  = self.$injector.get('AnnotationValueType');
      self.AnnotationTypeViewer = self.$injector.get('AnnotationTypeViewer');
      self.factory         = self.$injector.get('factory');

      self.annotationTypes = _.map(
        _.values(self.AnnotationValueType),
        function(valueType) {
          return new self.AnnotationType(
            self.factory.annotationType(self.study, { valueType: valueType }));
        });
    }));

    describe('for Participant Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.annotationTypeIdsInUse = [ this.annotationTypes[0].uniqueId ];
        context.annotationTypeName   = 'ParticipantAnnotationType';
        context.viewStateName = 'home.admin.studies.study.participants.annotationTypeView';
      });

      sharedBehaviour(context);
    });

    describe('for Specimen Link Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.annotationTypeIdsInUse = [ this.annotationTypes[0].uniqueId ];
        context.annotationTypeName   = 'SpecimenLinkAnnotationType';
        context.viewStateName = 'home.admin.studies.study.processing.spcLinkAnnotationTypeView';
      });

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      describe('(shared)', function() {

        beforeEach(inject(function($rootScope, $compile, templateMixin, testUtils) {
          var self = this;

          _.extend(self, templateMixin);

          self.state                    = self.$injector.get('$state');
          self.studyAnnotationTypeUtils = self.$injector.get('studyAnnotationTypeUtils');
          self.AnnotationTypeViewer     = self.$injector.get('AnnotationTypeViewer');

          self.putHtmlTemplates(
            '/assets/javascripts/admin/studies/directives/annotationTypes/studyAnnotationTypesTable/studyAnnotationTypesTable.html',
            '/assets/javascripts/common/directives/updateRemoveButtons.html');

          spyOn(self.state, 'go');

          self.modificationsAllowed = true;
          self.onRemove = jasmine.createSpy('onRemove');
          self.createController = createController;

          function createController() {
            self.element = angular.element([
              '<study-annotation-types-table',
              '   annotation-types="vm.annotationTypes"',
              '   annotation-type-name="vm.annotationTypeName"',
              '   annotation-type-ids-in-use="vm.annotationTypeIdsInUse"',
              '   view-state-name="vm.viewStateName"',
              '   modifications-allowed="vm.modificationsAllowed"',
              '   on-remove="vm.onRemove"',
              '</study-annotation-types-table>'
            ].join(''));

            self.scope = $rootScope.$new();
            self.scope.vm = {
              study:                  self.study,
              annotationTypes:        self.annotationTypes,
              annotationTypeIdsInUse: context.annotationTypeIdsInUse,
              annotationTypeName:     context.annotationTypeName,
              modificationsAllowed:   self.modificationsAllowed,
              viewStateName:          context.viewStateName,
              onRemove:               self.onRemove
            };

            $compile(self.element)(self.scope);
            self.scope.$digest();
            self.controller = self.element.controller('studyAnnotationTypesTable');
          }
        }));

        it('has valid scope', function() {
          this.createController();
          expect(this.controller.annotationTypes).toEqual(this.annotationTypes);
          expect(this.controller.annotationTypeIdsInUse).toEqual(context.annotationTypeIdsInUse);
          expect(this.controller.tableParams).not.toBeNull();
          expect(this.controller.modificationsAllowed).toEqual(this.modificationsAllowed);
          expect(this.controller.viewStateName).toEqual(context.viewStateName);
        });

        it('has valid table columns', function() {
          this.createController();
          expect(this.controller.columns).toBeArrayOfSize(4);
          expect(this.controller.columns[0].title).toBe('Name');
          expect(this.controller.columns[1].title).toBe('Type');
          expect(this.controller.columns[2].title).toBe('Required');
          expect(this.controller.columns[3].title).toBe('Description');
        });

        it('calling information should open an AnnotationTypeViewer', function() {
          this.createController();
          expect(this.controller.information(this.controller.annotationTypes[0]))
            .toEqual(jasmine.any(this.AnnotationTypeViewer));
        });

        it('on update should open a modal to display annotation type in use modal', function() {
          spyOn(this.modalService, 'modalOk');
          this.createController();
          this.controller.update(this.controller.annotationTypes[0]);
          expect(this.modalService.modalOk).toHaveBeenCalled();
        });

        it('on update should change state to update an annotation type', function() {
          var annotationTypeToUpdate = this.annotationTypes[1];
          spyOn(this.modalService, 'modalOk');
          this.createController();
          this.controller.update(annotationTypeToUpdate);
          expect(this.state.go).toHaveBeenCalledWith(
            context.viewStateName,
            { annotationTypeId: annotationTypeToUpdate.uniqueId });
        });

        it('on remove opens a modal to display annotation type in use modal', function() {
          spyOn(this.modalService, 'modalOk');
          this.createController();
          this.controller.remove(this.annotationTypes[0]);
          expect(this.modalService.modalOk).toHaveBeenCalled();
        });

        it('can remove an annotation type', function() {
          var annotTypeToRemove = this.annotationTypes[1];

          spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when(true));
          this.onRemove = jasmine.createSpy('onRemove').and.returnValue(this.$q.when(true));

          this.createController();
          this.controller.remove(annotTypeToRemove);
          this.scope.$digest();
          expect(this.onRemove).toHaveBeenCalledWith(annotTypeToRemove);
        });

        it('update fails for a disabled study', function() {
          var self = this;

          self.modificationsAllowed = false;
          self.createController();
          expect(function () {
            self.controller.update(self.annotationTypes[1]);
          }).toThrow(new Error('modifications not allowed'));
        });

        it('remove fails for a disabled study', function() {
          var self = this;

          self.modificationsAllowed = false;
          self.createController();
          expect(function () {
            self.controller.remove(self.annotationTypes[1]);
          }).toThrow(new Error('modifications not allowed'));

        });

      });
    }

  });

});
