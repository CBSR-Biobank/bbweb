/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['moment', 'underscore'], function(moment, _) {
  'use strict';

  DateTimeAnnotationFactory.$inject = ['Annotation', 'timeService'];

  function DateTimeAnnotationFactory(Annotation, timeService) {

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function DateTimeAnnotation(obj, annotationType, required) {
      var self = this,
          defaults = {
            annotationTypeId     : null
          };

      obj = obj || {};
      _.extend(self, defaults, _.pick(obj, _.keys(defaults)));
      Annotation.call(this, annotationType, required);

      _.extend(self, timeService.stringToDateAndTime(obj.stringValue));
    }

    DateTimeAnnotation.prototype = Object.create(Annotation.prototype);

    /**
     * Must return a string.
     */
    DateTimeAnnotation.prototype.getValue = function () {
      return timeService.dateAndTimeToDisplayString(this.date, this.time);
    };

    /**
     * date part is kept in this.date and time in this.time,
     *
     * they must be combined
     */
    DateTimeAnnotation.prototype.getServerAnnotation = function () {
      return {
        annotationTypeId: this.getAnnotationTypeId(),
        stringValue:      timeService.dateAndTimeToUtcString(this.date, this.time),
        selectedValues:   []
      };
    };

    return DateTimeAnnotation;
  }

  return DateTimeAnnotationFactory;
});
