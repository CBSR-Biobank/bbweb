/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
import moment from 'moment';

/**
 * Originally taken from link below and then modified.
 *
 * @return {AngularJSFilter} the factory function for this filter.
 */
/* @ngInject */
export default function timeagoFilterFactory(gettextCatalog) {
  return timeago;

  /*
   * @param time a Date
   */
  function timeago(time) {
    if(!time) {
      return gettextCatalog.getString('never');
    }
    return moment(time).fromNow();
  }

}
