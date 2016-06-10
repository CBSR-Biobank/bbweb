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

  describe('Directive: studyAnnotationTypesPanelDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($window,
                               $compile,
                               $rootScope,
                               templateMixin,
                               testUtils) {
      var self = this;

      _.extend(self, templateMixin);

      self.Study                      = self.$injector.get('Study');
      self.SpecimenLinkAnnotationType = self.$injector.get('SpecimenLinkAnnotationType');
      self.AnnotationType             = self.$injector.get('AnnotationType');
      self.AnnotationValueType        = self.$injector.get('AnnotationValueType');
      self.factory               = self.$injector.get('factory');
      self.Panel                      = self.$injector.get('Panel');
      self.$state                     = self.$injector.get('$state');

      self.annotationTypes = _.map(
        _.values(self.AnnotationValueType),
        function(valueType) {
          return new self.AnnotationType(
            self.factory.annotationType({ valueType: valueType }));
        });
      self.annotationTypeIdsInUse    = [ self.annotationTypes[0].uniqueId ] ;
      self.annotationTypeName        = 'ParticipantAnnotationType';
      self.modificationsAllowed      = true;
      self.panelId                   = 'study.panel.participantAnnotationTypes';
      self.addStateName              = 'home.admin.studies.study.participants.annotationTypeAdd';
      self.viewStateName             = 'home.admin.studies.study.participants.annotationTypeView';
      self.annotationTypeDescription = 'annotation type description';

      spyOn(self.$state, 'go').and.callFake(function () {});
      spyOn(self.Panel.prototype, 'add').and.callThrough();

      $window.localStorage.setItem(self.panelId, '');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/annotationTypes/studyAnnotationTypesPanel/studyAnnotationTypesPanel.html',
        '/assets/javascripts/admin/studies/directives/annotationTypes/studyAnnotationTypesTable/studyAnnotationTypesTable.html',
        '/assets/javascripts/common/directives/panelButtons.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html');

      this.createController = createController;

      function createController() {
        self.element = angular.element([
          '<uib-accordion close-others="false">',
          '  <study-annotation-types-panel',
          '     annotation-types="vm.annotationTypes"',
          '     annotation-type-ids-in-use="vm.annotationTypeIdsInUse"',
          '     annotation-type-description="' + self.annotationTypeDescription + '"',
          '     annotation-type-name="' + self.annotationTypeName + '"',
          '     panel-id="' + self.panelId + '"',
          '     modifications-allowed="vm.modificationsAllowed"',
          '     add-state-name="' + self.addStateName + '"',
          '     view-state-name="' + self.viewStateName + '"',
          '     on-remove="vm.onRemove">',
          '  </study-annotation-types-panel>',
          '</uib-accordion>'
        ].join(''));

        self.onRemove = jasmine.createSpy('onRemove');

        self.scope = $rootScope.$new();
        self.scope.vm = {
          annotationTypes:        self.annotationTypes,
          annotationTypeIdsInUse: self.annotationTypeIdsInUse,
          modificationsAllowed:   self.modificationsAllowed,
          onRemove:               self.onRemove
        };

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.find('study-annotation-types-panel')
          .controller('studyAnnotationTypesPanel');
      }
    }));

    it('has valid scope', function () {
      this.createController();
      expect(this.controller.annotationTypes).toEqual(this.annotationTypes);
      expect(this.controller.annotationTypeIdsInUse).toEqual(this.annotationTypeIdsInUse);
      expect(this.controller.annotationTypeDescription).toEqual(this.annotationTypeDescription);
      expect(this.controller.panelId).toEqual(this.panelId);
      expect(this.controller.modificationsAllowed).toEqual(this.modificationsAllowed);
      expect(this.controller.addStateName).toEqual(this.addStateName);
      expect(this.controller.viewStateName).toEqual(this.viewStateName);
    });

    it('should invoke panel add function', function() {
      this.createController();
      this.controller.add();
      expect(this.$state.go).toHaveBeenCalledWith(this.addStateName);
    });

    it('should call correct function on remove', function() {
      var annotationType = this.annotationTypes[0];
      this.createController();
      this.controller.onAnnotTypeRemove(annotationType);
      expect(this.onRemove).toHaveBeenCalledWith(annotationType);
    });


  });


});
